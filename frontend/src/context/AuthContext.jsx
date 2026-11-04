import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authApi, tokenStorage } from '../services/api'

const USER_KEY = 'driveflow.usuario'
const AuthContext = createContext(null)

function lerUsuario() {
  try {
    const salvo = localStorage.getItem(USER_KEY)
    return salvo && tokenStorage.get() ? JSON.parse(salvo) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(lerUsuario)

  const logout = useCallback(() => {
    tokenStorage.clear()
    localStorage.removeItem(USER_KEY)
    setUsuario(null)
  }, [])

  const login = useCallback(async (loginInformado, senha) => {
    const resp = await authApi.login(loginInformado, senha)
    tokenStorage.set(resp.token)
    localStorage.setItem(USER_KEY, JSON.stringify(resp.usuario))
    setUsuario(resp.usuario)
    return resp.usuario
  }, [])

  const atualizarUsuario = useCallback((parcial) => {
    setUsuario((atual) => {
      const novo = { ...atual, ...parcial }
      localStorage.setItem(USER_KEY, JSON.stringify(novo))
      return novo
    })
  }, [])

  useEffect(() => {
    const onUnauthorized = () => logout()
    window.addEventListener('driveflow:unauthorized', onUnauthorized)
    return () => window.removeEventListener('driveflow:unauthorized', onUnauthorized)
  }, [logout])

  const value = useMemo(() => ({
    usuario,
    autenticado: !!usuario,
    isCliente: usuario?.perfil === 'CLIENTE',
    isAgente: usuario?.perfil === 'AGENTE',
    isBanco: usuario?.tipoAgente === 'BANCO',
    login,
    logout,
    atualizarUsuario,
  }), [usuario, login, logout, atualizarUsuario])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  return ctx
}
