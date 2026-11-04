import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { automovelApi, pedidoApi } from '../services/api'
import Alert from '../components/Alert'
import Icon from '../components/Icon'
import { hojeISO, nomeCarro } from '../utils/format'

/** Introdução (cliente) e modificação (cliente ou agente) de pedidos de aluguel. */
export default function PedidoForm() {
  const { id } = useParams()
  const edicao = !!id
  const navigate = useNavigate()

  const [automoveis, setAutomoveis] = useState([])
  const [form, setForm] = useState({
    automovelId: '',
    dataInicio: hojeISO(),
    dataFim: '',
    observacoes: '',
    solicitaCredito: false,
    valorCreditoSolicitado: '',
  })
  const [erro, setErro] = useState('')
  const [campos, setCampos] = useState({})
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    const carregar = async () => {
      try {
        const disponiveis = await automovelApi.listar(true)
        if (edicao) {
          const p = await pedidoApi.buscar(id)
          // mantém o automóvel atual do pedido na lista, mesmo que esteja indisponível
          const lista = disponiveis.some((a) => a.id === p.automovel.id) ? disponiveis : [p.automovel, ...disponiveis]
          setAutomoveis(lista)
          setForm({
            automovelId: String(p.automovel.id),
            dataInicio: p.dataInicio,
            dataFim: p.dataFim,
            observacoes: p.observacoes || '',
            solicitaCredito: p.solicitaCredito,
            valorCreditoSolicitado: p.valorCreditoSolicitado ?? '',
          })
        } else {
          setAutomoveis(disponiveis)
        }
      } catch (e) {
        setErro(e.message)
      }
    }
    carregar()
  }, [id, edicao])

  const set = (campo) => (e) => {
    const valor = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm((f) => ({ ...f, [campo]: valor }))
  }

  const submit = async (e) => {
    e.preventDefault()
    setErro('')
    setCampos({})
    setEnviando(true)
    const dados = {
      ...form,
      automovelId: Number(form.automovelId),
      valorCreditoSolicitado: form.solicitaCredito ? Number(form.valorCreditoSolicitado) : null,
    }
    try {
      const salvo = edicao ? await pedidoApi.atualizar(id, dados) : await pedidoApi.criar(dados)
      navigate(`/pedidos/${salvo.id}`, { replace: true })
    } catch (err) {
      setErro(err.message)
      setCampos(err.campos || {})
    } finally {
      setEnviando(false)
    }
  }

  const erroCampo = (c) => campos[c] && <small className="field-error">{campos[c]}</small>
  const selecionado = automoveis.find((a) => String(a.id) === form.automovelId)

  return (
    <>
      <header className="page-header">
        <div>
          <Link to={edicao ? `/pedidos/${id}` : '/pedidos'} className="back-link">
            <Icon name="arrowLeft" size={16} /> Voltar
          </Link>
          <h1>{edicao ? `Modificar pedido #${id}` : 'Novo pedido de aluguel'}</h1>
          <p className="muted">
            {edicao
              ? 'Altere o automóvel, o período ou as observações do pedido.'
              : 'Escolha um automóvel disponível e o período desejado. O pedido será analisado por um agente.'}
          </p>
        </div>
      </header>

      <Alert>{erro}</Alert>

      <div className="split">
        <section className="card">
          <form className="form" onSubmit={submit}>
            <label>
              Automóvel
              <select value={form.automovelId} onChange={set('automovelId')} required>
                <option value="">Selecione…</option>
                {automoveis.map((a) => (
                  <option key={a.id} value={a.id}>
                    {nomeCarro(a)} — {a.placa}
                  </option>
                ))}
              </select>
              {erroCampo('automovelId')}
            </label>

            <div className="grid-2">
              <label>
                Data de início
                <input type="date" value={form.dataInicio} onChange={set('dataInicio')} min={edicao ? undefined : hojeISO()} required />
                {erroCampo('dataInicio')}
              </label>
              <label>
                Data de fim
                <input type="date" value={form.dataFim} onChange={set('dataFim')} min={form.dataInicio} required />
                {erroCampo('dataFim')}
              </label>
            </div>

            <label>
              Observações
              <textarea rows={3} value={form.observacoes} onChange={set('observacoes')} maxLength={500} placeholder="Ex.: retirada no aeroporto de Confins" />
            </label>

            <label className="checkbox">
              <input type="checkbox" checked={form.solicitaCredito} onChange={set('solicitaCredito')} />
              Desejo associar este aluguel a um contrato de crédito bancário
            </label>

            {form.solicitaCredito && (
              <label>
                Valor de crédito desejado (R$)
                <input type="number" min="1" step="0.01" value={form.valorCreditoSolicitado} onChange={set('valorCreditoSolicitado')} required />
              </label>
            )}

            <div className="form-actions">
              <button className="btn btn-primary" disabled={enviando}>
                {enviando ? 'Salvando…' : edicao ? 'Salvar alterações' : 'Enviar pedido'}
              </button>
            </div>
          </form>
        </section>

        <aside className="card car-preview">
          <div className="car-art">
            <Icon name="car" size={56} />
          </div>
          {selecionado ? (
            <>
              <h3>{selecionado.marca} {selecionado.modelo}</h3>
              <dl className="dl">
                <dt>Ano</dt><dd>{selecionado.ano}</dd>
                <dt>Placa</dt><dd className="mono">{selecionado.placa}</dd>
                <dt>Matrícula</dt><dd className="mono">{selecionado.matricula}</dd>
              </dl>
            </>
          ) : (
            <p className="muted">Selecione um automóvel para ver os detalhes.</p>
          )}
        </aside>
      </div>
    </>
  )
}
