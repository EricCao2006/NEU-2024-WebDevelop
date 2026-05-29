export function useDialog() {
    const dialog = (window as any).__dialog__

    if (!dialog) {
        console.warn('Dialog not initialized')
        return {
            show: (title: string, message: string) => {
                alert(`${title}\n${message}`)
            }
        }
    }

    return {
        show: (title: string, message: string) => dialog.show(title, message)
    }
}