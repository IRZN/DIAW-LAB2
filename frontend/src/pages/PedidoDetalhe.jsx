import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { pedidoApi } from '../services/api'
import StatusBadge from '../components/StatusBadge'
import Alert from '../components/Alert'
import Icon from '../components/Icon'
import { STATUS, TIPO_PROPRIETARIO, formatarData, formatarDataHora, formatarMoeda, nomeCarro } from '../utils/format'

const ETAPAS = ['PENDENTE', 'APROVADO', 'CONTRATADO']

export default function PedidoDetalhe() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isCliente, isAgente } = useAuth()
  const [pedido, setPedido] = useState(null)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  useEffect(() => {
    pedidoApi.buscar(id).then(setPedido).catch((e) => setErro(e.message))
  }, [id])

  const executar = async (acao, mensagem) => {
    setErro('')
    setSucesso('')
    try {
      const atualizado = await acao()
      setPedido(atualizado)
      setSucesso(mensagem)
    } catch (e) {
      setErro(e.message)
    }
  }

  const cancelar = () => {
    if (window.confirm('Deseja realmente cancelar este pedido?')) {
      executar(() => pedidoApi.cancelar(id), 'Pedido cancelado.')
    }
  }

  if (!pedido) {
    return erro ? <Alert>{erro}</Alert> : <p className="muted">Carregando…</p>
  }

  const p = pedido
  const podeModificar = isCliente ? p.status === 'PENDENTE' : ['PENDENTE', 'APROVADO'].includes(p.status)
  const podeCancelar = isCliente && ['PENDENTE', 'APROVADO'].includes(p.status)

  return (
    <>
      <header className="page-header">
        <div>
          <button className="back-link" onClick={() => navigate(-1)}>
            <Icon name="arrowLeft" size={16} /> Voltar
          </button>
          <h1>
            Pedido #{p.id} <StatusBadge status={p.status} />
          </h1>
          <p className="muted">{STATUS[p.status]?.descricao}</p>
        </div>
        <div className="header-actions">
          {podeModificar && (
            <Link to={`/pedidos/${p.id}/editar`} className="btn btn-ghost">
              <Icon name="edit" /> Modificar
            </Link>
          )}
          {podeCancelar && (
            <button className="btn btn-danger" onClick={cancelar}>
              <Icon name="x" /> Cancelar pedido
            </button>
          )}
        </div>
      </header>

      <Alert>{erro}</Alert>
      <Alert tipo="sucesso">{sucesso}</Alert>

      <Linha status={p.status} />

      <div className="split">
        <div className="stack">
          <section className="card">
            <h2>Aluguel</h2>
            <dl className="dl dl-2">
              <dt>Automóvel</dt><dd>{nomeCarro(p.automovel)}</dd>
              <dt>Placa</dt><dd className="mono">{p.automovel.placa}</dd>
              <dt>Matrícula</dt><dd className="mono">{p.automovel.matricula}</dd>
              <dt>Período</dt><dd>{formatarData(p.dataInicio)} a {formatarData(p.dataFim)} · {p.quantidadeDias} dias</dd>
              <dt>Crédito bancário</dt>
              <dd>{p.solicitaCredito ? `Solicitado · ${formatarMoeda(p.valorCreditoSolicitado)}` : 'Não solicitado'}</dd>
              <dt>Observações</dt><dd>{p.observacoes || '—'}</dd>
              <dt>Criado em</dt><dd>{formatarDataHora(p.criadoEm)}</dd>
              <dt>Atualizado em</dt><dd>{formatarDataHora(p.atualizadoEm)}</dd>
            </dl>
          </section>

          {(p.parecer || p.agenteAvaliador) && (
            <section className="card">
              <h2>Análise financeira</h2>
              <dl className="dl dl-2">
                <dt>Agente</dt><dd>{p.agenteAvaliador}</dd>
                <dt>Parecer</dt><dd>{p.parecer}</dd>
              </dl>
            </section>
          )}

          {p.contrato && (
            <section className="card">
              <h2>Contrato</h2>
              <dl className="dl dl-2">
                <dt>Executado por</dt><dd>{p.contrato.agenteNome}</dd>
                <dt>Data</dt><dd>{formatarDataHora(p.contrato.dataAssinatura)}</dd>
                <dt>Proprietário registrado</dt><dd>{TIPO_PROPRIETARIO[p.contrato.tipoProprietario]} · {p.automovel.proprietarioNome}</dd>
              </dl>
              {p.contrato.credito && (
                <div className="credit-box">
                  <h3><Icon name="bank" /> Contrato de crédito</h3>
                  <dl className="dl dl-2">
                    <dt>Banco</dt><dd>{p.contrato.credito.bancoNome}</dd>
                    <dt>Valor</dt><dd>{formatarMoeda(p.contrato.credito.valor)}</dd>
                    <dt>Juros</dt><dd>{p.contrato.credito.taxaJurosMensal}% a.m.</dd>
                    <dt>Parcelas</dt><dd>{p.contrato.credito.numeroParcelas}x de {formatarMoeda(p.contrato.credito.valorParcela)}</dd>
                  </dl>
                </div>
              )}
            </section>
          )}
        </div>

        <div className="stack">
          <section className="card">
            <h2>Cliente</h2>
            <dl className="dl">
              <dt>Nome</dt><dd>{p.cliente.nome}</dd>
              <dt>CPF</dt><dd className="mono">{p.cliente.cpf}</dd>
              <dt>Profissão</dt><dd>{p.cliente.profissao || '—'}</dd>
              <dt>Renda mensal total</dt><dd><strong>{formatarMoeda(p.cliente.rendaTotal)}</strong></dd>
            </dl>
          </section>

          {isAgente && p.status === 'PENDENTE' && (
            <AvaliacaoPainel onAvaliar={(aprovado, parecer) =>
              executar(() => pedidoApi.avaliar(id, aprovado, parecer), aprovado ? 'Pedido aprovado.' : 'Pedido reprovado.')} />
          )}

          {isAgente && p.status === 'APROVADO' && (
            <ContratoPainel pedido={p} onExecutar={(dados) =>
              executar(() => pedidoApi.executarContrato(id, dados), 'Contrato executado com sucesso.')} />
          )}
        </div>
      </div>
    </>
  )
}

function Linha({ status }) {
  const terminalNegativo = status === 'REPROVADO' || status === 'CANCELADO'
  const atual = ETAPAS.indexOf(status)
  return (
    <ol className={`timeline ${terminalNegativo ? 'timeline-stopped' : ''}`}>
      {ETAPAS.map((etapa, i) => (
        <li key={etapa} className={i <= atual ? 'done' : ''}>
          <span className="dot">{i <= atual ? <Icon name="check" size={14} /> : i + 1}</span>
          {STATUS[etapa].label}
        </li>
      ))}
      {terminalNegativo && (
        <li className="stopped">
          <span className="dot"><Icon name="x" size={14} /></span>
          {STATUS[status].label}
        </li>
      )}
    </ol>
  )
}

function AvaliacaoPainel({ onAvaliar }) {
  const [parecer, setParecer] = useState('')
  const [enviando, setEnviando] = useState(false)

  const avaliar = async (aprovado) => {
    if (!parecer.trim()) return
    setEnviando(true)
    await onAvaliar(aprovado, parecer)
    setEnviando(false)
  }

  return (
    <section className="card card-action">
      <h2>Avaliar pedido</h2>
      <p className="muted small">Registre o parecer da análise financeira do contratante.</p>
      <textarea
        rows={4}
        value={parecer}
        onChange={(e) => setParecer(e.target.value)}
        placeholder="Ex.: Renda comprovada compatível com o valor do aluguel."
        maxLength={500}
      />
      <div className="btn-row">
        <button className="btn btn-success" disabled={enviando || !parecer.trim()} onClick={() => avaliar(true)}>
          <Icon name="check" /> Parecer positivo
        </button>
        <button className="btn btn-danger" disabled={enviando || !parecer.trim()} onClick={() => avaliar(false)}>
          <Icon name="x" /> Parecer negativo
        </button>
      </div>
    </section>
  )
}

function ContratoPainel({ pedido, onExecutar }) {
  const { isBanco } = useAuth()
  const [tipo, setTipo] = useState('CLIENTE')
  const [comCredito, setComCredito] = useState(pedido.solicitaCredito)
  const [credito, setCredito] = useState({
    valor: pedido.valorCreditoSolicitado ?? '',
    taxaJurosMensal: '1.5',
    numeroParcelas: '12',
  })
  const [enviando, setEnviando] = useState(false)

  const bloqueado = pedido.solicitaCredito && !isBanco

  const submit = async (e) => {
    e.preventDefault()
    setEnviando(true)
    await onExecutar({
      tipoProprietario: tipo,
      credito: isBanco && comCredito
        ? {
            valor: Number(credito.valor),
            taxaJurosMensal: Number(credito.taxaJurosMensal),
            numeroParcelas: Number(credito.numeroParcelas),
          }
        : null,
    })
    setEnviando(false)
  }

  return (
    <section className="card card-action">
      <h2>Executar contrato</h2>
      {bloqueado ? (
        <p className="muted small">
          Este pedido solicita crédito bancário: o contrato deve ser executado por um <strong>banco</strong> agente.
        </p>
      ) : (
        <form className="form" onSubmit={submit}>
          <label>
            Registrar o automóvel como propriedade de
            <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
              <option value="CLIENTE">Cliente ({pedido.cliente.nome})</option>
              {isBanco ? <option value="BANCO">Banco (este agente)</option> : <option value="EMPRESA">Empresa (este agente)</option>}
            </select>
          </label>

          {isBanco && (
            <>
              <label className="checkbox">
                <input type="checkbox" checked={comCredito} disabled={pedido.solicitaCredito}
                  onChange={(e) => setComCredito(e.target.checked)} />
                Conceder contrato de crédito
              </label>
              {comCredito && (
                <div className="grid-3">
                  <label>
                    Valor (R$)
                    <input type="number" min="1" step="0.01" required value={credito.valor}
                      onChange={(e) => setCredito({ ...credito, valor: e.target.value })} />
                  </label>
                  <label>
                    Juros (% a.m.)
                    <input type="number" min="0" step="0.01" required value={credito.taxaJurosMensal}
                      onChange={(e) => setCredito({ ...credito, taxaJurosMensal: e.target.value })} />
                  </label>
                  <label>
                    Parcelas
                    <input type="number" min="1" max="120" required value={credito.numeroParcelas}
                      onChange={(e) => setCredito({ ...credito, numeroParcelas: e.target.value })} />
                  </label>
                </div>
              )}
            </>
          )}

          <button className="btn btn-primary btn-block" disabled={enviando}>
            {enviando ? 'Executando…' : 'Executar contrato'}
          </button>
        </form>
      )}
    </section>
  )
}
