const form = document.querySelector("#calculator-form");
const hoursInput = document.querySelector("#hours");
const rateInput = document.querySelector("#hourly-rate");
const errorMessage = document.querySelector("#form-error");
const results = document.querySelector("#results");

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  errorMessage.textContent = "";

  const hours = Number(hoursInput.value);
  const hourlyRate = Number(rateInput.value);

  if (!hoursInput.value || !Number.isFinite(hours) || hours < 0 || hours > 168) {
    errorMessage.textContent = "Enter hours between 0 and 168.";
    hoursInput.focus();
    return;
  }

  if (!rateInput.value || !Number.isFinite(hourlyRate) || hourlyRate < 0) {
    errorMessage.textContent = "Enter a valid hourly pay rate.";
    rateInput.focus();
    return;
  }

  const button = form.querySelector("button");
  button.disabled = true;
  button.querySelector("span").textContent = "Calculating...";

  try {
    const response = await fetch("/api/calculate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hours, hourlyRate }),
    });
    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.error || "Unable to calculate right now.");
    }

    document.querySelector("#net-pay").textContent = currency.format(data.netPay).replace("$", "");
    document.querySelector("#gross-pay").textContent = currency.format(data.grossPay);
    document.querySelector("#taxes").textContent = currency.format(data.taxes);
    document.querySelector("#take-home").textContent = currency.format(data.netPay);
    results.hidden = false;
    results.scrollIntoView({ behavior: "smooth", block: "nearest" });
  } catch (error) {
    errorMessage.textContent = error.message;
  } finally {
    button.disabled = false;
    button.querySelector("span").textContent = "Calculate my paycheck";
  }
});