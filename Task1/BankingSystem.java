import java.util.Scanner;

class BankAccount {
    private String accountNumber;
    private double balance;

    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be greater than zero.");
            return;
        }
        balance += amount;
        System.out.println("✅ ₹" + amount + " deposited successfully. Current Balance: ₹" + balance);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be greater than zero.");
            return;
        }
        if (amount > balance) {
            System.out.println("❌ Insufficient balance! Transaction declined.");
            return;
        }
        balance -= amount;
        System.out.println("✅ ₹" + amount + " withdrawn successfully. Current Balance: ₹" + balance);
    }

    public void checkBalance() {
        System.out.println("💰 Current Balance: ₹" + balance);
    }
}

class Customer {
    private String name;
    private String customerId;
    private BankAccount account;

    public Customer(String name, String customerId) {
        this.name = name;
        this.customerId = customerId;
    }

    public void createAccount(String accountNumber, double initialBalance) {
        if (account != null) {
            System.out.println("❌ You already have an account.");
            return;
        }
        account = new BankAccount(accountNumber, initialBalance);
        System.out.println("✅ Account created successfully with balance ₹" + initialBalance);
    }

    public BankAccount getAccount() {
        return account;
    }
}

public class BankingSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("🏦 Welcome to the Banking System 🏦");
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.print("Enter your Customer ID: ");
        String customerId = sc.nextLine();

        Customer customer = new Customer(name, customerId);

        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Account Number: ");
                    String accNum = sc.next();
                    System.out.print("Enter Initial Balance: ");
                    double initialBalance = sc.nextDouble();
                    customer.createAccount(accNum, initialBalance);
                    break;

                case 2:
                    if (customer.getAccount() != null) {
                        System.out.print("Enter amount to deposit: ");
                        double depositAmount = sc.nextDouble();
                        customer.getAccount().deposit(depositAmount);
                    } else {
                        System.out.println("❌ Please create an account first.");
                    }
                    break;

                case 3:
                    if (customer.getAccount() != null) {
                        System.out.print("Enter amount to withdraw: ");
                        double withdrawAmount = sc.nextDouble();
                        customer.getAccount().withdraw(withdrawAmount);
                    } else {
                        System.out.println("❌ Please create an account first.");
                    }
                    break;

                case 4:
                    if (customer.getAccount() != null) {
                        customer.getAccount().checkBalance();
                    } else {
                        System.out.println("❌ Please create an account first.");
                    }
                    break;

                case 5:
                    System.out.println("✅ Thank you for using our banking system!");
                    sc.close();
                    return;

                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }
}
