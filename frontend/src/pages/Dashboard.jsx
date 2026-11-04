import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { pedidoApi } from '../services/api'
import StatusBadge from '../components/StatusBadge'
import Alert from '../components/Alert'
import Icon from '../components/Icon'
import { STATUS, formatarData, nomeCarro } from '../utils/format'

export default function Dashboard() {
  const { usuario, isCliente } = useAuth()
  const [pedidos, setPedidos] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    pedidoApi.listar().then(setPedidos).catch((e) => setErro(e.message))
  }, [])

  const contagem = Object.keys(STATUS).reduce((acc, s) => {
    acc[s] = pedidos?.filter((p) => p.status === s).length ?? 0
    return acc
  }, {})

  const destaque = isCliente
    ? pedidos?.slice(0, 5)
    : pedidos?.filter((p) => p.status === 'PENDENTE' || p.status === 'APROVADO').slice(0, 6)

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Painel</p>
          <h1>Olá, {usuario?.nome?.split(' ')[0]}</h1>
          <p className="muted">
            {isCliente
              ? 'Acompanhe o status dos seus pedidos de aluguel.'
              : 'Pedidos aguardando análise financeira ou execução de contrato.'}
          </p>
        </div>
        {isCliente && (
          <Link to="/pedidos/novo" className="btn btn-primary">
            <Icon name="plus" /> Novo pedido
          </Link>
        )}
      </header>

      <Alert>{erro}</Alert>

      <section className="stats">
        {Object.keys(STATUS).map((s) => (
          <Link key={s} to={`/pedidos?status=${s}`} className={`stat stat-${s.toLowerCase()}`}>
            <span>{STATUS[s].label}</span>
            <strong>{pedidos ? contagem[s] : '–'}</strong>
          </Link>
        ))}
      </section>

      <section className="card">
        <div className="card-header">
          <h2>{isCliente ? 'Pedidos recentes' : 'Fila de trabalho'}</h2>
          <Link to="/pedidos" className="link">Ver todos →</Link>
        </div>
        {!pedidos && !erro && <p className="muted">Carregando…</p>}
        {destaque?.length === 0 && (
          <div className="empty">
            {isCliente ? (
              <>Você ainda não tem pedidos. <Link to="/pedidos/novo">Crie o primeiro</Link>.</>
            ) : (
              'Nenhum pedido aguardando ação.'
            )}
          </div>
        )}
        {destaque?.length > 0 && (
          <ul className="pedido-list">
            {destaque.map((p) => (
              <li key={p.id}>
                <Link to={`/pedidos/${p.id}`}>
                  <div className="pedido-car">
                    <Icon name="car" size={22} />
                  </div>
                  <div className="pedido-info">
                    <strong>#{p.id} · {nomeCarro(p.automovel)}</strong>
                    <span>
                      {!isCliente && <>{p.cliente.nome} · </>}
                      {formatarData(p.dataInicio)} a {formatarData(p.dataFim)} ({p.quantidadeDias} dias)
                    </span>
                  </div>
                  <StatusBadge status={p.status} />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}
