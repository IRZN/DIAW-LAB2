// Camada de acesso à API REST do back-end Spring Boot.
const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '')
const TOKEN_KEY = 'driveflow.token'

export class ApiError extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem)
    this.status = status
    this.campos = campos || {}
  }
}

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = tokenStorage.get()
  if (auth && token) headers.Authorization = `Bearer ${token}`

  let resp
  try {
    resp = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Verifique se o back-end está em execução.')
  }

  if (resp.status === 204) return null
  const data = await resp.json().catch(() => null)

  if (!resp.ok) {
    if (resp.status === 401 && auth) {
      // token expirado ou inválido: força novo login
      window.dispatchEvent(new Event('driveflow:unauthorized'))
    }
    throw new ApiError(resp.status, data?.mensagem || `Erro ${resp.status}`, data?.campos)
  }
  return data
}

export const authApi = {
  login: (login, senha) => request('/auth/login', { method: 'POST', body: { login, senha }, auth: false }),
  registrarCliente: (dados) => request('/auth/registro/cliente', { method: 'POST', body: dados, auth: false }),
  registrarAgente: (dados) => request('/auth/registro/agente', { method: 'POST', body: dados, auth: false }),
  me: () => request('/auth/me'),
}

export const clienteApi = {
  listar: () => request('/clientes'),
  meusDados: () => request('/clientes/me'),
  buscar: (id) => request(`/clientes/${id}`),
  atualizar: (id, dados) => request(`/clientes/${id}`, { method: 'PUT', body: dados }),
  excluir: (id) => request(`/clientes/${id}`, { method: 'DELETE' }),
}

export const automovelApi = {
  listar: (apenasDisponiveis = false) => request(`/automoveis${apenasDisponiveis ? '?disponiveis=true' : ''}`),
  criar: (dados) => request('/automoveis', { method: 'POST', body: dados }),
  atualizar: (id, dados) => request(`/automoveis/${id}`, { method: 'PUT', body: dados }),
  excluir: (id) => request(`/automoveis/${id}`, { method: 'DELETE' }),
}

export const pedidoApi = {
  listar: (status) => request(`/pedidos${status ? `?status=${status}` : ''}`),
  buscar: (id) => request(`/pedidos/${id}`),
  criar: (dados) => request('/pedidos', { method: 'POST', body: dados }),
  atualizar: (id, dados) => request(`/pedidos/${id}`, { method: 'PUT', body: dados }),
  cancelar: (id) => request(`/pedidos/${id}/cancelar`, { method: 'PATCH' }),
  avaliar: (id, aprovado, parecer) => request(`/pedidos/${id}/avaliar`, { method: 'PATCH', body: { aprovado, parecer } }),
  executarContrato: (id, dados) => request(`/pedidos/${id}/contrato`, { method: 'POST', body: dados }),
}
