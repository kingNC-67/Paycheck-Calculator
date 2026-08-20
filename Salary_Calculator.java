import java.util.Scanner;

public class Salary_Calculator {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        final double TAX_RATE = .15;

        System.out.println("Enter your hourly wage.");
        double hourlyWage = input.nextDouble();

        System.out.println("Enter the hours you worked this week.");
        double hoursWorked = input.nextDouble();

        double grossPay = hourlyWage * hoursWorked;
        double taxes = grossPay * TAX_RATE;
        double netPay = grossPay - taxes;

        System.out.println("Hours worked: " + hoursWorked);
        System.out.println("Hourly wage: $" + hourlyWage);
        System.out.println("Gross Pay: $" + grossPay);
        System.out.println("Taxes: $" + taxes);
        System.out.println("Net Pay: $" + netPay);

        input.close();
    }
}