# Legislação moçambicana aplicada ao sistema

> **Aviso:** este documento resume a legislação consultada em **Outubro de 2026** para orientar o desenvolvimento.
> Não é parecer jurídico nem contabilístico. Antes de usar o sistema numa empresa real, confirme os pontos
> marcados com ⚠️ com um jurista, um contabilista certificado ou o INSS / Autoridade Tributária.

## 1. Diplomas consultados e onde entram no código

| Diploma | O que regula | Onde está no código |
|---|---|---|
| **Lei n.º 13/2023, de 25 de Agosto** (Lei do Trabalho) | Contrato, férias, feriados, idade mínima, recibo de vencimento | `ParametrosLegais`, `FeriasServico`, `FuncionarioServico.validar` |
| **Regulamento da Segurança Social Obrigatória / INSS** | Contribuições: **3%** trabalhador, **4%** empregador | `ParametrosLegais.INSS_*`, `Pagavel` |
| **Código do IRPS** (art. 65.º-A, Lei n.º 11/2025) | Retenção na fonte sobre trabalho dependente (tabela mensal) | `ParametrosLegais.IRPS_MENSAL`, `Pagavel.calcularIrps` |
| **Diplomas Ministeriais n.º 34 a 41/2026, de 20 de Maio** | Salários mínimos por sector, desde 1 de Abril de 2026 | `SectorActividade`, `FuncionarioServico.validarValores` |

## 2. Regras implementadas

### Lei do Trabalho (Lei n.º 13/2023)
| Regra | Artigo | Implementação |
|---|---|---|
| Idade mínima de admissão: **18 anos** (15-17 só com autorização especial) | art. 29.º | Validação da data de nascimento na admissão. A excepção 15-17 **não** é suportada. |
| Férias: **12 dias** no 1.º ano de trabalho, **30 dias** nos seguintes | art. 108.º | `FeriasServico.saldo` |
| Férias contam-se em **dias de calendário** (não dias úteis) | art. 108.º | `FeriasServico.diasDeGozo` |
| Feriados dentro das férias **não contam** como dias de férias | art. 111.º | `ParametrosLegais.ehFeriado` |
| Fraccionamento por acordo em períodos **não inferiores a 6 dias** | art. 109.º | Recusa pedidos < 6 dias, salvo o último saldo |
| Férias de um ano podem gozar-se nesse ano ou no seguinte | art. 107.º | Campo "ano de referência" |
| Feriados nacionais (9 fixos); se caírem ao domingo, o descanso passa para segunda | art. 105.º | `ParametrosLegais.FERIADOS` |
| Salário nunca inferior ao mínimo do sector | art. 117.º ⚠️ | Validação do salário base |
| Recibo de vencimento com identificação, período, remunerações e descontos | art. 123.º | `RelatorioServico.recibo` |

### Segurança social e impostos
- **INSS:** 3% descontado ao trabalhador; 4% a cargo da empresa (aparece como "custo para a empresa").
- **IRPS:** tabela mensal de retenção (0 dependentes). Isento até 20.250 MT; escalões 10% / 15% / 20% / 25% / 32%
  com parcelas fixas de 0 / 200 / 1.775 / 7.375 / 28.375 MT. Conferida contra a tabela publicada (ex.: 150.000 MT → 30.055 MT).
- **Salário líquido** = bruto − INSS − IRPS. O IRPS é calculado sobre (bruto − INSS do trabalhador).

### Salários mínimos (desde 1/Abril/2026)
Agricultura 7.072 · Pesca marítima e semi-industrial 7.063,22 · Indústria transformadora 10.622,50 ·
Energia/água (grandes) 12.775 · Energia/água (PME) 10.366 · Construção 8.652 · Serviços não financeiros 10.845 ·
Hotelaria e turismo 10.600 · Segurança privada 8.690 · Bancos e seguradoras 20.361,43 · Microfinanças 17.924,57 (MT/mês).

O sector da empresa escolhe-se em **Definições da empresa** (só Administrador).

## 3. ⚠️ Pontos a confirmar antes de uso real

1. **INSS reduz a base do IRPS?** As fontes divergem. O código assume que **sim** (`INSS_REDUZ_BASE_IRPS = true`).
   Mudar para `false` se o contabilista indicar o contrário.
2. **Dependentes:** a tabela do IRPS com dependentes reduz a parcela fixa, mas **não** foi implementada
   (usa-se a tabela de 0 dependentes, que retém um pouco mais). Para uma folha real é preciso acrescentar.
3. **Estagiários:** o modelo original isenta de INSS e a aplicação mantém essa escolha. Confirmar o regime
   contributivo de bolsas de estágio/aprendizagem.
4. **Tabela de salários mínimos parcial:** faltam sectores confirmados em fonte oficial (ex.: indústria extractiva,
   pesca de kapenta, panificação). Acrescentar em `SectorActividade`.
5. **Feriados decretados pontualmente** (pelo Governo, para um ano concreto) não estão na lista fixa.
6. **Férias:** assume-se 12 dias no ano civil da admissão e 30 nos seguintes. Contratos a prazo com menos de
   um ano têm regra própria (proporcional aos meses de serviço) que **não** está implementada.
7. **Numeração dos artigos:** conferida no texto oficial da Lei 13/2023 (Boletim da República, I Série, n.º 165),
   mas confirme se houver alterações posteriores.

## 4. Fora do âmbito (não implementado)
- Pagamento de **trabalho extraordinário**, nocturno e em dias de descanso/feriado (art. 99.º e seguintes; limites legais de horas extra).
- Licença de maternidade (90 dias) e de paternidade (7 dias), faltas, baixa médica.
- Cessação do contrato: indemnizações, aviso prévio, processo disciplinar.
- 13.º mês, subsídios, adiantamentos e limites de descontos no salário.
- Mapas/guias para entrega ao INSS e à Autoridade Tributária (a aplicação calcula, não submete).
- Trabalhadores estrangeiros (passaporte/DIRE, quotas) — o campo documento exige o formato do BI moçambicano.
- Protecção de dados pessoais: não foi feita análise jurídica específica. Medidas técnicas existentes:
  acesso por perfis, palavras-passe com PBKDF2, cada funcionário só vê os seus dados. Confirme as obrigações legais
  aplicáveis à sua empresa (consentimento, retenção, notificação).

## 5. Fontes
- Lei n.º 13/2023, de 25 de Agosto — Boletim da República, I Série, n.º 165 (texto oficial publicado em ts.gov.mz).
- INSS Moçambique — taxas contributivas e tabela de salários mínimos em vigor (inss.gov.mz).
- Conselho de Ministros, 28 de Abril de 2026 (AIM) e Diplomas Ministeriais 34-41/2026.
- Tabela mensal de retenção do IRPS 2026 (art. 65.º-A, Lei n.º 11/2025) e guias fiscais de 2026 (RSM, Moore).
