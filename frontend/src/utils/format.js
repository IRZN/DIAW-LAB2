export const STATUS = {
  PENDENTE: { label: 'Pendente', descricao: 'Aguardando análise financeira de um agente' },
  APROVADO: { label: 'Aprovado', descricao: 'Parecer positivo — aguardando execução do contrato' },
  REPROVADO: { label: 'Reprovado', descricao: 'Parecer financeiro negativo' },
  CANCELADO: { label: 'Cancelado', descricao: 'Cancelado pelo cliente' },
  CONTRATADO: { label: 'Contratado', descricao: 'Contrato executado' },
}

export const TIPO_PROPRIETARIO = {
  CLIENTE: 'Cliente',
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
}

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export const formatarMoeda = (v) => (v === null || v === undefined || v === '' ? '—' : moeda.format(Number(v)))

export function formatarData(iso) {
  if (!iso) return '—'
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${ano}`
}

export function formatarDataHora(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

export function hojeISO() {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 10)
}

export function mascaraCpf(v) {
  return v
    .replace(/\D/g, '')
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

export function mascaraCnpj(v) {
  return v
    .replace(/\D/g, '')
    .slice(0, 14)
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1/$2')
    .replace(/(\d{4})(\d{1,2})$/, '$1-$2')
}

export const nomeCarro = (a) => (a ? `${a.marca} ${a.modelo} ${a.ano}` : '—')
