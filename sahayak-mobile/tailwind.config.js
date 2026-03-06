/** @type {import('tailwindcss').Config} */
module.exports = {
  // NativeWind uses this to generate styles
  content: ["./App.{js,jsx,ts,tsx}", "./src/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        // Sahayak theme colors (converted from oklch to hex)
        background: "#ffffff",
        foreground: "#1a1a1a",
        card: "#ffffff",
        "card-foreground": "#1a1a1a",
        primary: "#030213",
        "primary-foreground": "#ffffff",
        secondary: "#f0f0f5",
        "secondary-foreground": "#030213",
        muted: "#ececf0",
        "muted-foreground": "#717182",
        accent: "#e9ebef",
        "accent-foreground": "#030213",
        destructive: "#d4183d",
        "destructive-foreground": "#ffffff",
        border: "rgba(0, 0, 0, 0.1)",
        input: "#f3f3f5",
        ring: "#b3b3b3",
        // Sahayak brand colors
        sahayak: {
          blue: "#2563eb",
          green: "#16a34a",
          orange: "#ea580c",
          red: "#dc2626",
        },
      },
      fontFamily: {
        sans: ["System"],
      },
    },
  },
  plugins: [],
};

