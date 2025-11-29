/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/cljs/**/*.cljs",    // Onde estão seus componentes Reagent
        "./resources/html/index.html"
    ],
    theme: {
        extend: {},
    },
    plugins: [],
}