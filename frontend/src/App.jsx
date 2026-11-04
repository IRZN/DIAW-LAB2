import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Cadastro from './pages/Cadastro'
import Dashboard from './pages/Dashboard'
import Pedidos from './pages/Pedidos'
import PedidoForm from './pages/PedidoForm'
import PedidoDetalhe from './pages/PedidoDetalhe'
import Automoveis from './pages/Automoveis'
import Clientes from './pages/Clientes'
import Perfil from './pages/Perfil'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="pedidos" element={<Pedidos />} />
          <Route path="pedidos/:id" element={<PedidoDetalhe />} />
          <Route path="pedidos/:id/editar" element={<PedidoForm />} />
          <Route path="automoveis" element={<Automoveis />} />

          <Route element={<ProtectedRoute perfil="CLIENTE" />}>
            <Route path="pedidos/novo" element={<PedidoForm />} />
            <Route path="perfil" element={<Perfil />} />
          </Route>

          <Route element={<ProtectedRoute perfil="AGENTE" />}>
            <Route path="clientes" element={<Clientes />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
