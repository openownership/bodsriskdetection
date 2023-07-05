const colors = require('tailwindcss/colors')

module.exports = {
    content: ['src/web/webapp/**/*.peb', 'src/web/css/**/*.css', 'src/web/js/**/*.js'],
    theme: {
        container: {
            center: true,
        },
        fontFamily: {
            app: 'Inter',
            title: 'Playfair Display',
            mono: 'JetBrains Mono',
        },
        colors: {
            amber: colors.amber,
            blue: colors.blue,
            emerald: colors.emerald,
            gray: colors.gray,
            green: colors.green,
            lime: colors.lime,
            orange: colors.orange,
            red: colors.red,
            sky: colors.sky,
            slate: colors.slate,
            stone: colors.stone,
            teal: colors.teal,
            yellow: colors.yellow,

            copy: colors.slate['700'],
            control: '#3B25D8',
            'control-darkbg': '#14BDEB',
            header: '#343A40',
            white: '#FFFFFF',
            label: colors.slate['500'],
            page: colors.stone['100']
        }
    },
    plugins: []
}
