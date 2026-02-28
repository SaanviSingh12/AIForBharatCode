import React from 'react';
import { useNavigate, useLocation } from 'react-router';
import { Home, Heart, Stethoscope, Pill, User } from 'lucide-react';

export const BottomNav: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { path: '/home', icon: Home, label: 'Home' },
    { path: '/symptom-entry', icon: Heart, label: 'Symptoms' },
    { path: '/doctor-search', icon: Stethoscope, label: 'Doctors' },
    { path: '/prescription-search', icon: Pill, label: 'Pharmacy' },
    { path: '/profile', icon: User, label: 'Profile' },
  ];

  // Don't show on language selection or emergency mode
  if (location.pathname === '/' || location.pathname === '/emergency') {
    return null;
  }

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <div className="flex items-center justify-around px-2 py-2 max-w-md mx-auto">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path || 
                          (item.path === '/doctor-search' && location.pathname.startsWith('/doctor/'));
          const Icon = item.icon;
          
          return (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`flex flex-col items-center justify-center px-3 py-2 rounded-lg transition-all ${
                isActive
                  ? 'text-blue-600'
                  : 'text-gray-600 hover:text-blue-500'
              }`}
            >
              <Icon className={`w-6 h-6 mb-1 ${isActive ? 'fill-blue-600' : ''}`} />
              <span className="text-xs font-medium">{item.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
