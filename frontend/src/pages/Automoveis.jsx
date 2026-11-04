import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { automovelApi } from '../services/api'
import Alert from '../components/Alert'
import Icon from '../components/Icon'
import { TIPO_PROPRIETARIO } from '../utils/format'

const VAZIO = { matricula: '', ano: new Date().getFullYear(), marca: '', modelo: '', placa: '' }

export default function Automoveis() {
  const { isAgente } = useAuth()
  const [lista, setLista] = useState(null)
  const [erro, setErro] = useState('')
  const [editando, setEditando] = useState(null) // null = fechado; {} = novo; {id..} = edição

  const carregar = () => automovelApi.listar().then(setLista).catch((e) => setErro(e.message))
  useEffect(() => { carregar() }, [])

  const excluir = async (a) => {
    if (!window.confirm(`Excluir ${a.marca} ${a.modelo} (${a.placa})?`)) return
    try {
      await automovelApi.excluir(a.id)
      carregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Frota</p>
          <h1>Automóveis</h1>
          <p className="muted">Matrícula, ano, marca, modelo, placa e proprietário registrado.</p>
        </div>
        {isAgente && !editando && (
          <button className="btn btn-primary" onClick={() => setEditando(VAZIO)}>
            <Icon name="plus" /> Novo automóvel
          </button>
        )}
      </header>

      <Alert>{erro}</Alert>

      {editando && (
        <AutomovelForm
          inicial={editando}
          onCancelar={() => setEditando(null)}
          onSalvo={() => {
            setEditando(null)
            carregar()
          }}
        />
      )}

      {!lista && !erro && <p className="muted">Carregando…</p>}
      <section className="car-grid">
        {lista?.map((a) => (
          <article key={a.id} className={`car-card ${a.disponivel ? '' : 'car-card-off'}`}>
            <div className="car-card-top">
              <span className={`pill ${a.disponivel ? 'pill-ok' : 'pill-off'}`}>
                {a.disponivel ? 'Disponível' : 'Alugado'}
              </span>
              <span className="plate">{a.placa}</span>
            </div>
            <div className="car-art small"><Icon name="car" size={40} /></div>
            <h3>{a.marca} {a.modelo}</h3>
            <dl className="dl dl-compact">
              <dt>Ano</dt><dd>{a.ano}</dd>
              <dt>Matrícula</dt><dd className="mono">{a.matricula}</dd>
              <dt>Proprietário</dt>
              <dd>{a.tipoProprietario ? `${TIPO_PROPRIETARIO[a.tipoProprietario]} · ${a.proprietarioNome}` : '—'}</dd>
            </dl>
            {isAgente && (
              <div className="card-actions">
                <button className="icon-btn" title="Editar" onClick={() => setEditando(a)}><Icon name="edit" /></button>
                <button className="icon-btn danger" title="Excluir" onClick={() => excluir(a)}><Icon name="trash" /></button>
              </div>
            )}
          </article>
        ))}
      </section>
    </>
  )
}

function AutomovelForm({ inicial, onCancelar, onSalvo }) {
  const [d, setD] = useState({ ...VAZIO, ...inicial })
  const [erro, setErro] = useState('')
  const [campos, setCampos] = useState({})
  const set = (c) => (e) => setD({ ...d, [c]: e.target.value })
  const erroCampo = (c) => campos[c] && <small className="field-error">{campos[c]}</small>

  const submit = async (e) => {
    e.preventDefault()
    setErro('')
    setCampos({})
    const dados = { matricula: d.matricula, ano: Number(d.ano), marca: d.marca, modelo: d.modelo, placa: d.placa }
    try {
      if (inicial.id) await automovelApi.atualizar(inicial.id, dados)
      else await automovelApi.criar(dados)
      onSalvo()
    } catch (err) {
      setErro(err.message)
      setCampos(err.campos || {})
    }
  }

  return (
    <section className="card">
      <h2>{inicial.id ? 'Editar automóvel' : 'Novo automóvel'}</h2>
      <Alert>{erro}</Alert>
      <form className="form" onSubmit={submit}>
        <div className="grid-3">
          <label>Marca<input value={d.marca} onChange={set('marca')} required />{erroCampo('marca')}</label>
          <label>Modelo<input value={d.modelo} onChange={set('modelo')} required />{erroCampo('modelo')}</label>
          <label>Ano<input type="number" min="1950" max="2100" value={d.ano} onChange={set('ano')} required />{erroCampo('ano')}</label>
          <label>Placa<input value={d.placa} onChange={set('placa')} required placeholder="ABC1D23" />{erroCampo('placa')}</label>
          <label>Matrícula (RENAVAM)<input value={d.matricula} onChange={set('matricula')} required />{erroCampo('matricula')}</label>
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-ghost" onClick={onCancelar}>Cancelar</button>
          <button className="btn btn-primary">Salvar</button>
        </div>
      </form>
    </section>
  )
}
