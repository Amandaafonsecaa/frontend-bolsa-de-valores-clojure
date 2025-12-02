module.exports = {
    darkMode: "class",
    content: [
        "./src/cljs/**/*.{js,jsx,ts,tsx,cljs,cljc,clj}",
        "./resources/public/index.html"
    ],
    theme: {
        extend: {
            colors: {
                background: "hsl(var(--background) / <alpha-value>)",
                foreground: "hsl(var(--foreground) / <alpha-value>)",

                card: "hsl(var(--card) / <alpha-value>)",
                popover: "hsl(var(--popover) / <alpha-value>)",

                primary: "hsl(var(--primary) / <alpha-value>)",
                "primary-foreground": "hsl(var(--primary-foreground) / <alpha-value>)",

                secondary: "hsl(var(--secondary) / <alpha-value>)",
                "secondary-foreground": "hsl(var(--secondary-foreground) / <alpha-value>)",

                muted: "hsl(var(--muted) / <alpha-value>)",
                "muted-foreground": "hsl(var(--muted-foreground) / <alpha-value>)",

                accent: "hsl(var(--accent) / <alpha-value>)",
                "accent-foreground": "hsl(var(--accent-foreground) / <alpha-value>)",

                success: "hsl(var(--success) / <alpha-value>)",
                danger: "hsl(var(--danger) / <alpha-value>)",

                border: "hsl(var(--border) / <alpha-value>)",
                input: "hsl(var(--input) / <alpha-value>)",
                ring: "hsl(var(--ring) / <alpha-value>)",
            },
            boxShadow: {
                glow: "var(--shadow-glow)",
            },
            transition: {
                smooth: "var(--transition-smooth)",
            }
        },
    },
    plugins: [],
};
