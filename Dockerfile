FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN mkdir -p .build && javac -d .build SalaryCalculatorServer.java

CMD ["java", "-cp", ".build", "SalaryCalculatorServer"]
