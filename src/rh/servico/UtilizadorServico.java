package rh.servico;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import rh.excecao.AutenticacaoException;
import rh.excecao.RecursoNaoEncontradoException;
import rh.excecao.RegraNegocioException;
import rh.excecao.ValidacaoException;
import rh.modelo.*;
import rh.repositorio.Repositorio;
import rh.util.Senha;

/** Autenticação e gestão das contas de acesso. */
public class UtilizadorServico {
    public static final int MAX_TENTATIVAS = 5;
    private static final String SAL_FALSO = Senha.novoSal();

    private final Repositorio<Utilizador> repo;
    private final Repositorio<Funcionario> funcionarios;

    public UtilizadorServico(Repositorio<Utilizador> repo, Repositorio<Funcionario> funcionarios) {
        this.repo = repo;
        this.funcionarios = funcionarios;
    }

    /** Valida as credenciais. A mensagem de erro é sempre a mesma para não revelar se o utilizador existe. */
    public Utilizador autenticar(String nome, String password) {
        Optional<Utilizador> o = procurar(nome);
        if (o.isEmpty()) {
            Senha.hash(password == null ? "" : password, SAL_FALSO); // gasta o mesmo tempo
            throw new AutenticacaoException("Utilizador ou palavra-passe incorrectos.");
        }
        Utilizador u = o.get();
        if (!u.isAtivo()) throw new AutenticacaoException("Conta desactivada. Contacte o administrador.");
        if (u.getTentativasFalhadas() >= MAX_TENTATIVAS)
            throw new AutenticacaoException("Conta bloqueada após " + MAX_TENTATIVAS
                    + " tentativas falhadas. Peça ao administrador para redefinir a palavra-passe.");
        if (!Senha.confere(password == null ? "" : password, u.getSal(), u.getHash())) {
            u.setTentativasFalhadas(u.getTentativasFalhadas() + 1);
            repo.atualizar(u);
            throw new AutenticacaoException("Utilizador ou palavra-passe incorrectos.");
        }
        if (u.getPerfil() == Perfil.FUNCIONARIO && !funcionarioActivo(u.getFuncionarioId()))
            throw new AutenticacaoException("O funcionário associado a esta conta está inactivo.");
        u.setTentativasFalhadas(0);
        u.setUltimoAcesso(LocalDateTime.now());
        repo.atualizar(u);
        return u;
    }

    public Utilizador criar(String nome, String password, Perfil perfil, int funcionarioId, boolean mudarPassword) {
        validarNome(nome);
        if (procurar(nome).isPresent()) throw new RegraNegocioException("Já existe um utilizador com este nome.");
        Senha.validarForca(password);
        validarAssociacao(perfil, funcionarioId, -1);
        String sal = Senha.novoSal();
        Utilizador u = new Utilizador(nome.trim().toLowerCase(), sal, Senha.hash(password, sal), perfil, funcionarioId);
        u.setMudarPassword(mudarPassword);
        return repo.criar(u);
    }

    public Utilizador atualizar(int id, Perfil perfil, int funcionarioId, boolean ativo) {
        Utilizador u = obter(id);
        validarAssociacao(perfil, funcionarioId, id);
        boolean eraAdminActivo = u.getPerfil() == Perfil.ADMINISTRADOR && u.isAtivo();
        boolean continuaAdminActivo = perfil == Perfil.ADMINISTRADOR && ativo;
        if (eraAdminActivo && !continuaAdminActivo && outrosAdminsActivos(id) == 0)
            throw new RegraNegocioException("Tem de existir pelo menos um administrador activo.");
        u.setPerfil(perfil);
        u.setFuncionarioId(funcionarioId);
        u.setAtivo(ativo);
        if (ativo) u.setTentativasFalhadas(0);
        repo.atualizar(u);
        return u;
    }

    /** Administrador redefine: gera palavra-passe temporária, que o utilizador terá de mudar no próximo acesso. */
    public String redefinirPassword(int id) {
        Utilizador u = obter(id);
        String temp = Senha.temporaria();
        String sal = Senha.novoSal();
        u.definirPassword(sal, Senha.hash(temp, sal));
        u.setMudarPassword(true);
        u.setTentativasFalhadas(0);
        repo.atualizar(u);
        return temp;
    }

    public void alterarPassword(int id, String atual, String nova) {
        Utilizador u = obter(id);
        if (!Senha.confere(atual == null ? "" : atual, u.getSal(), u.getHash()))
            throw new AutenticacaoException("A palavra-passe actual está incorrecta.");
        Senha.validarForca(nova);
        if (Senha.confere(nova, u.getSal(), u.getHash()))
            throw new RegraNegocioException("A nova palavra-passe tem de ser diferente da actual.");
        String sal = Senha.novoSal();
        u.definirPassword(sal, Senha.hash(nova, sal));
        u.setMudarPassword(false);
        repo.atualizar(u);
    }

    public List<Utilizador> listar() { return repo.listarTodos(); }
    public Optional<Utilizador> buscar(int id) { return repo.buscarPorId(id); }

    private Optional<Utilizador> procurar(String nome) {
        if (nome == null) return Optional.empty();
        String n = nome.trim();
        return repo.listarTodos().stream().filter(u -> u.getNomeUtilizador().equalsIgnoreCase(n)).findFirst();
    }

    private Utilizador obter(int id) {
        return repo.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Utilizador não encontrado."));
    }

    private long outrosAdminsActivos(int idIgnorado) {
        return repo.listarTodos().stream()
                .filter(u -> u.getId() != idIgnorado && u.isAtivo() && u.getPerfil() == Perfil.ADMINISTRADOR).count();
    }

    private boolean funcionarioActivo(int id) {
        return funcionarios.buscarPorId(id).map(Funcionario::isAtivo).orElse(false);
    }

    private static void validarNome(String nome) {
        if (nome == null || !nome.trim().matches("[A-Za-z0-9._-]{2,30}"))
            throw new ValidacaoException("Nome de utilizador: 2 a 30 caracteres (letras, números, ponto, hífen ou _).");
    }

    /** Um perfil Funcionário tem de estar ligado a um funcionário; um funcionário só tem uma conta. */
    private void validarAssociacao(Perfil perfil, int funcionarioId, int idIgnorado) {
        if (perfil == null) throw new ValidacaoException("Escolha um perfil.");
        if (funcionarioId != Utilizador.SEM_FUNCIONARIO) {
            if (funcionarios.buscarPorId(funcionarioId).isEmpty())
                throw new ValidacaoException("O funcionário associado não existe.");
            boolean jaTemConta = repo.listarTodos().stream()
                    .anyMatch(u -> u.getId() != idIgnorado && u.getFuncionarioId() == funcionarioId);
            if (jaTemConta) throw new RegraNegocioException("Este funcionário já tem uma conta de acesso.");
        } else if (perfil == Perfil.FUNCIONARIO) {
            throw new ValidacaoException("O perfil Funcionário tem de estar associado a um funcionário.");
        }
    }
}
