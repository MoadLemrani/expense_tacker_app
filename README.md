# 💰 Expense Tracker App (JavaFX)

A desktop expense tracking application built using **JavaFX**, designed to help users manage their monthly budget, visualize spending habits, and track savings or debt.

## 🚀 Features

- 📊 **Pie Chart Visualizations** by category
- 🧾 **Add, Modify, Delete Expenses** with ease
- 💡 **Auto-save monthly data** in `.txt` files
- 📈 **Budget progress bar** and savings/debt indicators
- 🔁 **Auto-reset** when a new month starts
- ⚡ **Splash screen** on startup
- 🎯 **Input validation and user-friendly UI**

---

## 🧠 Core Functionalities

| Feature | Description |
|--------|-------------|
| **Splash Screen** | Displays an FXML-based splash screen with a 3-second delay |
| **Add/Modify/Delete** | Expenses are added or updated using `TextField` and `ComboBox`. Modification pre-fills the form |
| **TableView** | Displays a list of expenses with live updates and action buttons |
| **PieChart** | Shows a percentage breakdown of expenses by category and allows category filtering on click |
| **ProgressBar** | Indicates percentage of budget spent; updates dynamically |
| **Savings/Debt Display** | Compares budget to expenses and shows remaining savings or overspending (debt) |
| **Persistent Storage** | Saves/loads data from `expenses_YYYY_MM.txt` using custom file parsing |
| **Monthly Budget** | Updates budget live, triggers savings/debt recalculation and progress bar update |
| **Auto Month Check** | Detects if the current month has changed and clears old data accordingly |

---

## 📂 Project Structure

```text
ExpenseTrackerApp/
├── src/
│   └── main/
│       ├── java/com/mycompany/expensetrackerapp/
│       │   ├── ExpenseTrackerApp.java
│       │   ├── Expense.java
│       │   └── SplashController.java
│       └── resources/
│           ├── splash.fxml
│           ├── splash_screen.png
│           └── styles.css
├── expenses_2025_06.txt       # Example saved monthly file
├── pom.xml                    # Maven configuration
├── .gitignore
└── README.md

---

## 🧩 Technologies Used

- **Java 17+**
- **JavaFX 17 (Controls, FXML, Graphics)**
- **FontAwesomeFX** for UI icons
- **FXML** for the splash screen layout
- **Maven** for dependency management

---

##⚙️ How to Run

1. Clone the repository
2. Open the project in **NetBeans**, **IntelliJ**, or any JavaFX-compatible IDE
3. Make sure JavaFX is configured (or use the Maven dependencies)
4. Run the `ExpenseTrackerApp.java` main class

---

## 📌 Notes

- **Data is saved per month** in `expenses_YYYY_MM.txt`
- **All fields are required** to add an expense
- **Changing the budget** live updates the savings, debt, and progress bar

---

## 🙌 Author

Made with ❤️ by Moad LEMRANI as a personal finance utility and JavaFX learning project.