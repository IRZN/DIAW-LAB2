import { Fragment, useEffect, useState } from 'react'
import { clienteApi } from '../services/api'
import Alert from '../components/Alert'
import { formatarDataHora, formatarMoeda } from '../utils/format'

/** Consulta de clientes (agentes) — apoio à análise financeira dos pedidos. */
export default function Clientes() {
  const [clientes, setClientes] = useState(null)
  const [aberto, setAberto] = useState(null)
  const [busca, setBusca] = useState('')
  const [erro, setErro] = useState('')

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch((e) => setErro(e.message))
  }, [])

  const filtrados = clientes?.filter((c) =>
    `${c.nome} ${c.cpf} ${c.profissao || ''}`.toLowerCase().includes(busca.toLowerCase()),
  )

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Contratantes</p>
          <h1>Clientes</h1>
          <p className="muted">Dados de identificação, profissão e rendimentos declarados.</p>
        </div>
        <input className="search" placeholder="Buscar por nome, CPF ou profissão" value={busca} onChange={(e) => setBusca(e.target.value)} />
      </header>

      <Alert>{erro}</Alert>

      <section className="card table-card">
        {!clientes && !erro && <p className="muted pad">Carregando…</p>}
        {filtrados?.length === 0 && <div className="empty">Nenhum cliente encontrado.</div>}
        {filtrados?.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>RG</th>
                  <th>Profissão</th>
                  <th>Renda total</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {filtrados.map((c) => (
                  <Fragment key={c.id}>
                    <tr>
                      <td><strong>{c.nome}</strong><div className="muted small">@{c.login}</div></td>
                      <td className="mono">{c.cpf}</td>
                      <td className="mono">{c.rg}</td>
                      <td>{c.profissao || '—'}</td>
                      <td>{formatarMoeda(c.rendaTotal)}</td>
                      <td>
                        <button className="link" onClick={() => setAberto(aberto === c.id ? null : c.id)}>
                          {aberto === c.id ? 'Ocultar' : 'Detalhes'}
                        </button>
                      </td>
                    </tr>
                    {aberto === c.id && (
                      <tr className="row-detail">
                        <td colSpan={6}>
                          <div className="detail-grid">
                            <div>
                              <h4>Endereço</h4>
                              <p>{c.endereco}</p>
                              <p className="muted small">Cliente desde {formatarDataHora(c.criadoEm)}</p>
                            </div>
                            <div>
                              <h4>Rendimentos ({c.rendimentos.length}/3)</h4>
                              {c.rendimentos.length === 0 && <p className="muted">Nenhum rendimento informado.</p>}
                              <ul className="plain">
                                {c.rendimentos.map((r, i) => (
                                  <li key={i}>{r.entidadeEmpregadora} — <strong>{formatarMoeda(r.valor)}</strong></li>
                                ))}
                              </ul>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
