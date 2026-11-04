import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { clienteApi } from '../services/api'
import ClienteForm from '../components/ClienteForm'
import Alert from '../components/Alert'
import Icon from '../components/Icon'

/** Read / Update / Delete do próprio cadastro do cliente. */
export default function Perfil() {
  const { usuario, atualizarUsuario, logout } = useAuth()
  const navigate = useNavigate()
  const [cliente, setCliente] = useState(null)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [campos, setCampos] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    clienteApi.meusDados().then(setCliente).catch((e) => setErro(e.message))
  }, [])

  const salvar = async (dados) => {
    setErro('')
    setSucesso('')
    setCampos({})
    setEnviando(true)
    try {
      const atualizado = await clienteApi.atualizar(usuario.id, dados)
      setCliente(atualizado)
      setVersao((v) => v + 1)
      atualizarUsuario({ nome: atualizado.nome, login: atualizado.login })
      setSucesso('Cadastro atualizado com sucesso.')
    } catch (e) {
      setErro(e.message)
      setCampos(e.campos || {})
    } finally {
      setEnviando(false)
    }
  }

  const excluirConta = async () => {
    if (!window.confirm('Excluir sua conta permanentemente? Esta ação não pode ser desfeita.')) return
    try {
      await clienteApi.excluir(usuario.id)
      logout()
      navigate('/login', { replace: true })
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Conta</p>
          <h1>Meu cadastro</h1>
          <p className="muted">Mantenha seus dados de identificação e rendimentos atualizados para agilizar a análise.</p>
        </div>
      </header>

      <Alert>{erro}</Alert>
      <Alert tipo="sucesso">{sucesso}</Alert>

      {!cliente && !erro && <p className="muted">Carregando…</p>}
      {cliente && (
        <section className="card">
          <ClienteForm
            key={versao}
            inicial={cliente}
            modoEdicao
            campos={campos}
            enviando={enviando}
            submitLabel="Salvar alterações"
            onSubmit={salvar}
          />
        </section>
      )}

      <section className="card danger-zone">
        <div>
          <h2>Excluir conta</h2>
          <p className="muted small">Só é possível excluir contas sem pedidos de aluguel vinculados.</p>
        </div>
        <button className="btn btn-danger" onClick={excluirConta}>
          <Icon name="trash" /> Excluir minha conta
        </button>
      </section>
    </>
  )
}
