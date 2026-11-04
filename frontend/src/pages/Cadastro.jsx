import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import ClienteForm from '../components/ClienteForm'
import Alert from '../components/Alert'
import { mascaraCnpj } from '../utils/format'

export default function Cadastro() {
  const [tipo, setTipo] = useState('CLIENTE')
  const [erro, setErro] = useState('')
  const [campos, setCampos] = useState({})
  const [enviando, setEnviando] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const executar = async (acao, credenciais) => {
    setErro('')
    setCampos({})
    setEnviando(true)
    try {
      await acao()
      await login(credenciais.login, credenciais.senha)
      navigate('/', { replace: true })
    } catch (err) {
      setErro(err.message)
      setCampos(err.campos || {})
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-page auth-page-wide">
      <section className="auth-card auth-card-wide">
        <Link to="/login" className="back-link">← Voltar ao login</Link>
        <h2>Criar conta</h2>
        <p className="muted">O sistema só pode ser utilizado após cadastro prévio.</p>

        <div className="segmented" role="tablist">
          <button className={tipo === 'CLIENTE' ? 'active' : ''} onClick={() => setTipo('CLIENTE')} type="button">
            Sou cliente
          </button>
          <button className={tipo === 'AGENTE' ? 'active' : ''} onClick={() => setTipo('AGENTE')} type="button">
            Sou agente (empresa ou banco)
          </button>
        </div>

        <Alert>{erro}</Alert>

        {tipo === 'CLIENTE' ? (
          <ClienteForm
            campos={campos}
            enviando={enviando}
            submitLabel="Criar conta de cliente"
            onSubmit={(dados) => executar(() => authApi.registrarCliente(dados), dados)}
          />
        ) : (
          <AgenteForm
            campos={campos}
            enviando={enviando}
            onSubmit={(dados) => executar(() => authApi.registrarAgente(dados), dados)}
          />
        )}
      </section>
    </div>
  )
}

function AgenteForm({ campos, enviando, onSubmit }) {
  const [d, setD] = useState({ login: '', senha: '', nome: '', cnpj: '', tipo: 'EMPRESA' })
  const set = (c) => (e) => setD({ ...d, [c]: c === 'cnpj' ? mascaraCnpj(e.target.value) : e.target.value })
  const erro = (c) => campos[c] && <small className="field-error">{campos[c]}</small>

  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault()
        onSubmit(d)
      }}
    >
      <fieldset>
        <legend>Acesso</legend>
        <div className="grid-2">
          <label>
            Usuário
            <input value={d.login} onChange={set('login')} required minLength={3} />
            {erro('login')}
          </label>
          <label>
            Senha
            <input type="password" value={d.senha} onChange={set('senha')} required minLength={6} />
            {erro('senha')}
          </label>
        </div>
      </fieldset>
      <fieldset>
        <legend>Dados do agente</legend>
        <label>
          Razão social / nome
          <input value={d.nome} onChange={set('nome')} required />
          {erro('nome')}
        </label>
        <div className="grid-2">
          <label>
            CNPJ
            <input value={d.cnpj} onChange={set('cnpj')} required placeholder="00.000.000/0000-00" />
            {erro('cnpj')}
          </label>
          <label>
            Tipo de agente
            <select value={d.tipo} onChange={set('tipo')}>
              <option value="EMPRESA">Empresa</option>
              <option value="BANCO">Banco</option>
            </select>
          </label>
        </div>
      </fieldset>
      <div className="form-actions">
        <button className="btn btn-primary" disabled={enviando}>
          {enviando ? 'Salvando…' : 'Criar conta de agente'}
        </button>
      </div>
    </form>
  )
}
