
// const appConfigElement = document.querySelector("app-configuration");
// appConfigElement.content = {
//   localizations: {
//     toArray: () => [
//       {
//         location: "en",
//         value: `{
//           "journey.blocked.title": " ",
//           "journey.blocked.subtitle": " ",
//           "journey.blocked.message": "Your access is unfortunately invalid - please make sure that you have correctly completed the purchase of your package. If you have any questions, please contact info@lustaufsfasten.at",
//           "journey.notStarted.title": "Welcome to your fasting experience!",
//           "journey.notStarted.subtitle": "We look forward to you! Your desire for fasting team!",
//           "journey.notStarted.message": "Your {dayCount}-day lust for fasting trip starts here on Wednesday, the {date, date}. If you want to watch our welcome video beforehand to get in the mood, then click here. You can start on Tuesday, {startsAtPrecise, date} at {nextDayUnlocksAt, time, short}, in advance view the content of your first day of preparation!",
//           "journey.started.title": "Welcome to your fasting experience!",
//           "journey.started.subtitle": " ",
//           "journey.started.message": "Your {dayCount}-day fasting trip can begin, we wish you a lot of fun!",
//           "journey.finished.title": "Welcome to your fasting experience!",
//           "journey.finished.subtitle": " ",
//           "journey.finished.message": "Your {dayCount}-day fasting trip is over!",
//           "journey.button.startYourDay": "Start your Day"
//         }`,
//       },
//       {
//         location: "de",
//         value: `{
//           "journey.blocked.title": " ",
//           "journey.blocked.subtitle": " ",
//           "journey.blocked.message": "Dein Zugang ist leider ungültig - bitte stelle sicher, dass du den Kauf deines Pakets richtig abgeschlossen hast.'</p><br /><p>'Bei Fragen, wende dich bitte an info@lustaufsfasten.at",
//           "journey.notStarted.title": "Herzlich willkommen zu deiner Fastenexperience!",
//           "journey.notStarted.subtitle": "Wir freuen uns auf Dich! Dein Lust aufs Fasten Team!",
//           "journey.notStarted.message": "Deine {dayCount} tägige Lust aufs Fasten Reise startet hier am Mittwoch, dem {date, date}. Willst du davor zur Einstimmung schon unser Willkommensvideo ansehen, dann klicke hier. Ab Dienstag, {startsAtPrecise, date} ab {nextDayUnlocksAt, time, short}, kannst du schon vorab die Inhalte von deinem ersten Vorbereitungstag ansehen!",
//           "journey.started.title": "Herzlich willkommen zu deiner Fastenexperience!",
//           "journey.started.subtitle": " ",
//           "journey.started.message": "Deine {dayCount}-tägige Fastenreise kann beginnen, wir wünschen dir viel Spaß!",
//           "journey.finished.title": "Herzlich willkommen zu deiner Fastenexperience!",
//           "journey.finished.subtitle": " ",
//           "journey.finished.message": "Deine {dayCount}-tägige Fastenreise ist beendet!",
//           "journey.button.startYourDay": "Beginne deinen Tag"
//         }`,
//       },
//     ],
//   },
// };

// const dynamicForm = document.querySelector("dynamic-form");
// let errors = dynamicForm.validationerrors || {};

// fetch("dynamic-form.css")
//   .then(response => response.text())
//   .then(css => (dynamicForm.css = css));

// fetch("dynamic-form-fields.json")
//   .then(response => response.json())
//   .then(formfields => (dynamicForm.formfields = formfields));

// dynamicForm.initialvalues = {
//   userNumber: 1,
//   userMessage: "hello",
//   month: "January"
// };

// dynamicForm.addEventListener("change", event => {
//   const { changes, previousValues, values } = event.detail;
//   errors = { ...errors };
//   errors.password =
//     !values.password || values.password.length < 8
//       ? "Password must be at least 8 characters long"
//       : null;
//   errors.repeatPassword = errors.password
//     ? null
//     : !values.repeatPassword
//     ? "Required field"
//     : values.password !== values.repeatPassword
//     ? "Password do not match"
//     : null;
//   const userNumber = Number(values.userNumber);
//   errors.userNumber = !Number.isFinite(userNumber)
//     ? "this field is required"
//     : userNumber <= 0 || userNumber > 100
//     ? "must be a positive number not bigger than 100"
//     : null;
//   dynamicForm.validationerrors = errors;
// });

// dynamicForm.addEventListener("submit", event =>
//   alert(JSON.stringify(event.detail, null, 2))
// );
// dynamicForm.addEventListener("discard", event => alert("discarded"));
