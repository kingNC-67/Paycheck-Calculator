# Salary Calculator

A free and publicly accessible Java paycheck calculator designed to help users estimate their weekly earnings based on hours worked and pay rate.

## About the Project

I created this project independently to showcase my Java programming while building something that could be useful to others.

The calculator takes information such as hours worked and pay rate and calculates estimated earnings.

## Features

- Calculates employee earnings
- Accepts user input
- Performs automatic calculations
- Displays formatted results
- Free to use
- Source code publicly available

## Technologies

- Java standard library (`HttpServer`) for the web server and calculator API
- HTML, CSS, and vanilla JavaScript for the browser interface
- GitHub

## Purpose

My goal was to create a simple and accessible tool that could help people better understand their estimated paycheck while strengthening my programming and problem-solving skills.

## Run on Replit

The Replit workflow compiles and starts `SalaryCalculatorServer.java` on port 5000. Open the web preview to use the calculator.

To run it locally:

```bash
mkdir -p .build
javac -d .build SalaryCalculatorServer.java
java -cp .build SalaryCalculatorServer
```

Then visit `http://localhost:5000`.

The original `Salary_Calculator.java` console program is intentionally kept in the project as the original Java work.
