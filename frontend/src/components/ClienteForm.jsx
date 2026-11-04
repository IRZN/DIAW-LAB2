import { useState } from 'react'
import { mascaraCpf } from '../utils/format'
import Icon from './Icon'

const MAX_RENDIMENTOS = 3

const VAZIO = {
  login: '',
  senha: '',
  nome: '',
  rg: '',
  cpf: '',
  endereco: '',
  profissao: '',
  rendimentos: [],
}

/**
 * Formulário de cliente usado no cadastro (Create) e na edição do próprio cadastro (Update).
 * Armazena dados de identificação, profissão e até 3 rendimentos.
 */
export default function ClienteForm({ inicial, modoEdicao = false, campos = {}, enviando, submitLabel, onSubmit }) {
  const [dados, setDados] = useState(() => ({
    ...VAZIO,
    ...inicial,
    senha: '',
    rendimentos: (inicial?.rendimentos || []).map((r) => ({ ...r, valor: String(r.valor) })),
  }))

  const set = (campo) => (e) => {
    const valor = campo === 'cpf' ? mascaraCpf(e.target.value) : e.target.value
    setDados((d) => ({ ...d, [campo]: valor }))
  }

  const setRendimento = (i, campo, valor) =>
    setDados((d) => ({
      ...d,
      rendimentos: d.rendimentos.map((r, idx) => (idx === i ? { ...r, [campo]: valor } : r)),
    }))

  const addRendimento = () =>
    setDados((d) => ({ ...d, rendimentos: [...d.rendimentos, { entidadeEmpregadora: '', valor: '' }] }))

  const removeRendimento = (i) =>
    setDados((d) => ({ ...d, rendimentos: d.rendimentos.filter((_, idx) => idx !== i) }))

  const submit = (e) => {
    e.preventDefault()
    onSubmit({
      ...dados,
      senha: dados.senha || null,
      rendimentos: dados.rendimentos
        .filter((r) => r.entidadeEmpregadora.trim() || r.valor)
        .map((r) => ({ entidadeEmpregadora: r.entidadeEmpregadora, valor: Number(r.valor || 0) })),
    })
  }

  const erro = (c) => campos[c] && <small className="field-error">{campos[c]}</small>

  return (
    <form onSubmit={submit} className="form">
      <fieldset>
        <legend>Acesso</legend>
        <div className="grid-2">
          <label>
            Usuário
            <input value={dados.login} onChange={set('login')} required minLength={3} autoComplete="username" />
            {erro('login')}
          </label>
          <label>
            {modoEdicao ? 'Nova senha (opcional)' : 'Senha'}
            <input
              type="password"
              value={dados.senha}
              onChange={set('senha')}
              required={!modoEdicao}
              minLength={6}
              autoComplete="new-password"
              placeholder={modoEdicao ? 'Deixe em branco para manter' : 'Mínimo 6 caracteres'}
            />
            {erro('senha')}
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Identificação</legend>
        <label>
          Nome completo
          <input value={dados.nome} onChange={set('nome')} required />
          {erro('nome')}
        </label>
        <div className="grid-2">
          <label>
            RG
            <input value={dados.rg} onChange={set('rg')} required />
            {erro('rg')}
          </label>
          <label>
            CPF
            <input value={dados.cpf} onChange={set('cpf')} required placeholder="000.000.000-00" inputMode="numeric" />
            {erro('cpf')}
          </label>
        </div>
        <label>
          Endereço
          <input value={dados.endereco} onChange={set('endereco')} required />
          {erro('endereco')}
        </label>
        <label>
          Profissão
          <input value={dados.profissao || ''} onChange={set('profissao')} />
        </label>
      </fieldset>

      <fieldset>
        <legend>
          Rendimentos <span className="muted">({dados.rendimentos.length}/{MAX_RENDIMENTOS})</span>
        </legend>
        {dados.rendimentos.length === 0 && <p className="muted small">Nenhum rendimento informado.</p>}
        {dados.rendimentos.map((r, i) => (
          <div className="rendimento-row" key={i}>
            <input
              placeholder="Entidade empregadora"
              value={r.entidadeEmpregadora}
              onChange={(e) => setRendimento(i, 'entidadeEmpregadora', e.target.value)}
              required
            />
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="Valor mensal (R$)"
              value={r.valor}
              onChange={(e) => setRendimento(i, 'valor', e.target.value)}
              required
            />
            <button type="button" className="icon-btn danger" onClick={() => removeRendimento(i)} aria-label="Remover rendimento">
              <Icon name="x" />
            </button>
          </div>
        ))}
        {erro('rendimentos')}
        <button type="button" className="btn btn-ghost" onClick={addRendimento} disabled={dados.rendimentos.length >= MAX_RENDIMENTOS}>
          <Icon name="plus" /> Adicionar rendimento
        </button>
      </fieldset>

      <div className="form-actions">
        <button className="btn btn-primary" disabled={enviando}>
          {enviando ? 'Salvando…' : submitLabel}
        </button>
      </div>
    </form>
  )
}
