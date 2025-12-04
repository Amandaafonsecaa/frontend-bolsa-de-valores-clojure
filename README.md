# 📈 BolsaFácil - Sistema de Gestão de Carteira de Ações

Sistema web desenvolvido em ClojureScript para gerenciamento de carteira de ações, permitindo consultar cotações, registrar compras e vendas, e acompanhar o desempenho dos investimentos.

## 🚀 Tecnologias

- **ClojureScript** - Linguagem de programação funcional
- **Reagent** - Biblioteca React para ClojureScript
- **Ring** - Framework web para Clojure
- **Compojure** - Roteamento web
- **Ajax** - Requisições HTTP

## 📋 Funcionalidades

### Dashboard
- Visualização do patrimônio líquido
- Valor total investido
- Cálculo automático de lucro/prejuízo
- Tabela com saldo por ativo

### Cotação de Ativos
- Consulta de cotações em tempo real
- Exibição de preço atual, variação percentual
- Dados completos: abertura, máximo, mínimo, fechamento e hora da cotação

### Transações
- Registro de compras de ações
- Registro de vendas de ações
- Validação de datas (não permite datas futuras)
- Integração com API de cotações

### Carteira
- Extrato completo de transações
- Filtros por período (data inicial e final)
- Resumo de totais: transações, comprado e vendido
- Histórico detalhado com preços e valores

## 🛠️ Instalação

### Pré-requisitos
- Java JDK 8 ou superior
- Leiningen (ferramenta de build para Clojure)

### Passos

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd frontend-bolsa-de-valores-clojure
```

2. Instale as dependências:
```bash
lein deps
```

3. Inicie o servidor de desenvolvimento:
```bash
lein figwheel
```

4. Acesse a aplicação em:
```
http://localhost:3449
```

## 📁 Estrutura do Projeto

```
src/
├── cljs/
│   └── bolsa_front/
│       ├── core.cljs          # Ponto de entrada e roteamento
│       ├── state.cljs         # Estado global da aplicação
│       ├── externals.cljs     # Conexões HTTP com backend
│       ├── layout.cljs        # Layout e navegação
│       ├── ajax.cljs         # Configuração de AJAX
│       └── pages/
│           ├── dashboard.cljs    # Página principal
│           ├── cotacao.cljs      # Consulta de cotações
│           ├── buysell.cljs      # Compras e vendas
│           ├── carteira.cljs     # Extrato da carteira
│           └── home.cljs         # Página de teste
└── clj/
    └── bolsa_front/
        └── routes/            # Rotas do servidor
```

## 🔌 API Backend

O frontend se conecta ao backend através da URL base:
```
http://localhost:3000
```

### Endpoints Utilizados

- `GET /carteira/extrato` - Lista de transações
- `GET /carteira/saldo` - Saldo por ativo
- `GET /carteira/investido` - Valor total investido
- `GET /carteira/patrimonio` - Patrimônio líquido
- `GET /cotacao/:ticker` - Cotação de uma ação
- `POST /transacoes/compra` - Registrar compra
- `POST /transacoes/venda` - Registrar venda

## 👥 Desenvolvedoras

<table>
<tr>
<td align="center">
  <a href="https://github.com/Amandaafonsecaa">
    <img src="https://github.com/Amandaafonsecaa.png" width="150" height="150" style="border-radius: 50%;"/>
  </a>
  <br />
  <b>Amanda Fonsêca</b>
  <br />
  <a href="https://github.com/Amandaafonsecaa">
    <img src="https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white" alt="GitHub"/>
  </a>
  <a href="https://www.linkedin.com/in/amanda-fonseca-b4189426b">
    <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white" alt="LinkedIn"/>
  </a>
</td>
<td align="center">
  <a href="https://github.com/lumab23">
    <img src="https://github.com/lumab23.png" width="150" height="150" style="border-radius: 50%;"/>
  </a>
  <br />
  <b>Luma Brandão</b>
  <br />
  <a href="https://github.com/lumab23">
    <img src="https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white" alt="GitHub"/>
  </a>
  <a href="https://www.linkedin.com/in/lbca23">
    <img src="https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white" alt="LinkedIn"/>
  </a>
</td>
</tr>
</table>

Este projeto foi desenvolvido como parte de um trabalho acadêmico.