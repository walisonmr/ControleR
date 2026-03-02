# ControleR - Gestão Financeira Pessoal 💰

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/Android-SDK%2034-green.svg?style=flat&logo=android)](https://developer.android.com/about/versions/14)
[![Version](https://img.shields.io/badge/Version-0.7-orange.svg)](https://github.com/ControleR)

O **ControleR** é um aplicativo Android moderno para controle de finanças pessoais, focado em simplicidade, eficiência e uma interface intuitiva. Ele permite que usuários acompanhem seus lançamentos, gerenciem dívidas e visualizem o panorama financeiro através de gráficos dinâmicos.

---

## 📸 Screenshots

[Screenshot aqui]
*Interface moderna utilizando Material 3 e Jetpack Compose.*

---

## ✨ Funcionalidades Implementadas

- ✅ **Dashboard Inteligente**: Visualização rápida do saldo total, despesas e receitas com gráficos comparativos.
- ✅ **Gestão de Lançamentos**: Registro detalhado de ganhos e gastos com categorias e datas.
- ✅ **Controle de Dívidas**: Monitoramento de débitos pendentes com status de pagamento.
- ✅ **Perfil Personalizado**: Edição de informações do usuário e metas financeiras.
- ✅ **Modo Dark/Light**: Suporte nativo a temas baseados nas preferências do sistema.
- ✅ **Persistência Local**: Todos os dados são salvos de forma segura no dispositivo.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Banco de Dados**: [Room](https://developer.android.com/training/data-storage/room)
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Navegação**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Gráficos**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Injeção/Gerenciamento**: ViewModelFactory para injeção de dependências manual.

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/com/example/financeiroapp/
├── data/
│   ├── dao/          # Interfaces de acesso ao banco (Room)
│   ├── database/     # Configuração da Database
│   └── model/        # Entidades de dados
├── ui/
│   ├── components/   # Componentes reutilizáveis do Compose
│   ├── navigation/   # Definições de rotas e navegação
│   ├── screens/      # Telas principais (Dashboard, Lançamentos, etc)
│   ├── theme/        # Definições de cores, fontes e tema
│   └── viewmodel/    # Lógica de negócio e estado da UI
└── MainActivity.kt    # Ponto de entrada do app
```

---

## 🚀 Como Clonar e Rodar

### Pré-requisitos
- Android Studio Iguana ou superior.
- JDK 17.
- Dispositivo Android ou Emulador (API 26+).

### Passos
1. Clone este repositório:
   ```bash
   git clone https://github.com/seu-usuario/ControleR.git
   ```
2. Abra o projeto no **Android Studio**.
3. Aguarde a sincronização do Gradle.
4. Conecte seu dispositivo ou inicie o emulador.
5. Clique em **Run** (ícone de play verde).

---

## 📌 Status do Projeto
**Versão Atual: 0.7 - Em desenvolvimento 🏗️**

O projeto está em fase ativa de evolução, com melhorias constantes na interface e adição de novas métricas financeiras.

---
*Desenvolvido com ❤️ por [Seu Nome/Link]*
