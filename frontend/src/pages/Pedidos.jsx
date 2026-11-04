import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { pedidoApi } from '../services/api'
import StatusBadge from '../components/StatusBadge'
import Alert from '../components/Alert'
import Icon from '../components/Icon'
import { STATUS, formatarData, formatarDataHora, nomeCarro } from '../utils/format'

export default function Pedidos() {
  const { isCliente } = useAuth()
  const [params, setParams] = useSearchParams()
  const status = params.get('status') || ''
  const [pedidos, setPedidos] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    setPedidos(null)
    pedidoApi.listar(status).then(setPedidos).catch((e) => setErro(e.message))
  }, [status])

  const filtrar = (s) => setParams(s ? { status: s } : {})

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Pedidos de aluguel</p>
          <h1>{isCliente ? 'Meus pedidos' : 'Todos os pedidos'}</h1>
        </div>
        {isCliente && (
          <Link to="/pedidos/novo" className="btn btn-primary">
            <Icon name="plus" /> Novo pedido
          </Link>
        )}
      </header>

      <div className="chips">
        <button className={!status ? 'chip active' : 'chip'} onClick={() => filtrar('')}>Todos</button>
        {Object.entries(STATUS).map(([k, v]) => (
          <button key={k} className={status === k ? 'chip active' : 'chip'} onClick={() => filtrar(k)}>
            {v.label}
          </button>
        ))}
      </div>

      <Alert>{erro}</Alert>

      <section className="card table-card">
        {!pedidos && !erro && <p className="muted pad">Carregando…</p>}
        {pedidos?.length === 0 && <div className="empty">Nenhum pedido encontrado.</div>}
        {pedidos?.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nº</th>
                  {!isCliente && <th>Cliente</th>}
                  <th>Automóvel</th>
                  <th>Período</th>
                  <th>Crédito</th>
                  <th>Criado em</th>
                  <th>Status</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {pedidos.map((p) => (
                  <tr key={p.id}>
                    <td className="mono">#{p.id}</td>
                    {!isCliente && <td>{p.cliente.nome}</td>}
                    <td>
                      {nomeCarro(p.automovel)}
                      <div className="muted small mono">{p.automovel.placa}</div>
                    </td>
                    <td>
                      {formatarData(p.dataInicio)} – {formatarData(p.dataFim)}
                      <div className="muted small">{p.quantidadeDias} dias</div>
                    </td>
                    <td>{p.solicitaCredito ? 'Sim' : 'Não'}</td>
                    <td className="muted">{formatarDataHora(p.criadoEm)}</td>
                    <td><StatusBadge status={p.status} /></td>
                    <td><Link className="link" to={`/pedidos/${p.id}`}>Detalhes</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
