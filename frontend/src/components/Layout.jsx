import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Icon from './Icon'

export default function Layout() {
  const { usuario, isCliente, isAgente, logout } = useAuth()
  const navigate = useNavigate()

  const sair = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const papel = isCliente ? 'Cliente' : usuario?.tipoAgente === 'BANCO' ? 'Agente · Banco' : 'Agente · Empresa'

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <img src="/logo.svg" alt="" width="36" height="36" />
          <div>
            <strong>DriveFlow</strong>
            <span>Aluguel de carros</span>
          </div>
        </div>

        <nav className="nav">
          <NavLink to="/" end><Icon name="home" /> Painel</NavLink>
          <NavLink to="/pedidos" end><Icon name="file" /> {isAgente ? 'Pedidos' : 'Meus pedidos'}</NavLink>
          {isCliente && <NavLink to="/pedidos/novo"><Icon name="plus" /> Novo pedido</NavLink>}
          <NavLink to="/automoveis"><Icon name="car" /> Automóveis</NavLink>
          {isAgente && <NavLink to="/clientes"><Icon name="users" /> Clientes</NavLink>}
          {isCliente && <NavLink to="/perfil"><Icon name="user" /> Meu cadastro</NavLink>}
        </nav>

        <div className="sidebar-user">
          <div className="avatar">{usuario?.nome?.[0]?.toUpperCase() || '?'}</div>
          <div className="who">
            <strong>{usuario?.nome}</strong>
            <span>{papel}</span>
          </div>
          <button className="icon-btn" onClick={sair} title="Sair" aria-label="Sair">
            <Icon name="logout" />
          </button>
        </div>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
