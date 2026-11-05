// App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, NavLink, Navigate, Link } from 'react-router-dom';
import { FindLocker } from './dashboards/findlocker';
import { TrackParcel } from './dashboards/trackparcel';
import { LockerTerminal } from './dashboards/lockerterminal';
import { OperatorDashboard } from './dashboards/operatordashboard';
import "./index.css"

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50 flex flex-col font-sans">
        {/* Navigation Bar */}
        <header className="bg-gray-900 text-white shadow-md sticky top-0 z-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16">
              {/* Brand Logo */}
              <Link to="/find" className="flex items-center gap-2 font-bold text-xl tracking-tight text-blue-400 hover:text-blue-300 transition-colors">
                <span>📦</span> TuttiBox
              </Link>

              {/* Navigation Links */}
              <nav className="flex gap-6 sm:gap-2">
                {[
                  { path: '/find', label: 'Find Locker' },
                  { path: '/track', label: 'Track Parcel' },
                  { path: '/terminal', label: 'Terminal' },
                  { path: '/operator', label: 'Operator' },
                ].map(({ path, label }) => (
                  <NavLink
                    key={path}
                    to={path}
                    className={({ isActive }) =>
                      `px-4 py-2 rounded-md text-sm font-medium transition-all ${
                        isActive
                          ? 'bg-gray-800 text-blue-400 shadow-inner'
                          : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                      }`
                    }
                  >
                    {label}
                  </NavLink>
                ))}
              </nav>
            </div>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<Navigate to="/find" replace />} />
            <Route path="/find" element={<FindLocker />} />
            <Route path="/track" element={<TrackParcel />} />
            <Route path="/terminal" element={<LockerTerminal />} />
            <Route path="/operator" element={<OperatorDashboard />} />
            
            {/* Styled 404 Fallback */}
            <Route
              path="*"
              element={
                <div className="text-center py-16 space-y-4">
                  <h1 className="text-6xl font-extrabold text-gray-300">404</h1>
                  <p className="text-xl text-gray-600 font-medium">Page Not Found</p>
                  <p className="text-gray-400 text-sm">The route you are looking for does not exist.</p>
                  <Link
                    to="/find"
                    className="inline-block mt-4 px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-sm"
                  >
                    Return to Home
                  </Link>
                </div>
              }
            />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
};

export default App;
