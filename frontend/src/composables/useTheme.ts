import { ref } from 'vue'

type Theme = 'light' | 'dark'

const theme = ref<Theme>((localStorage.getItem('theme') as Theme) || 'light')

export function useTheme() {
    const toggleTheme = () => {
        theme.value = theme.value === 'light' ? 'dark' : 'light'
        localStorage.setItem('theme', theme.value)
        applyTheme()
    }

    const applyTheme = () => {
        document.documentElement.setAttribute('data-theme', theme.value)
    }

    applyTheme()

    return { theme, toggleTheme }
}