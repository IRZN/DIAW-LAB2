import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Alert from '../components/Alert'

export default function Login() {
  const { login, autenticado } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ login: '', senha: '' })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (autenticado) return <Navigate to="/" replace />

  const submit = async (e) => {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await login(form.login, form.senha)
      navigate(location.state?.from || '/', { replace: true })
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <div className="brand brand-lg">
          <img src="/logo.svg" alt="" width="48" height="48" />
          <div>
            <strong>DriveFlow</strong>
            <span>Aluguel de carros</span>
          </div>
        </div>
        <h1>Do pedido ao contrato, em um só lugar.</h1>
        <p>
          Clientes introduzem e acompanham pedidos de aluguel. Empresas e bancos analisam,
          avaliam e executam os contratos — com crédito bancário quando necessário.
        </p>
        <ul className="hero-steps">
          <li><b>1</b> Cliente cria o pedido</li>
          <li><b>2</b> Agente faz a análise financeira</li>
          <li><b>3</b> Contrato executado</li>
        </ul>
      </section>

      <section className="auth-card">
        <h2>Entrar</h2>
        <p className="muted">Acesso restrito a usuários cadastrados.</p>
        <Alert>{erro}</Alert>
        <form onSubmit={submit} className="form">
          <label>
            Usuário
            <input
              autoFocus
              autoComplete="username"
              value={form.login}
              onChange={(e) => setForm({ ...form, login: e.target.value })}
              required
            />
          </label>
          <label>
            Senha
            <input
              type="password"
              autoComplete="current-password"
              value={form.senha}
              onChange={(e) => setForm({ ...form, senha: e.target.value })}
              required
            />
          </label>
          <button className="btn btn-primary btn-block" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
        <p className="auth-switch">
          Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
        </p>
        <div className="demo-hint">
          <strong>Contas de demonstração</strong> (senha <code>123456</code>):
          <code>cliente</code> · <code>locadora</code> · <code>banco</code>
        </div>
      </section>
    </div>
  )
}
