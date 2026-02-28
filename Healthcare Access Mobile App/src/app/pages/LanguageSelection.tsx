import React from 'react';
import { useNavigate } from 'react-router';
import { Globe, ChevronRight } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { languages } from '../data/mockData';
import { Card } from '../components/ui/card';

export const LanguageSelection: React.FC = () => {
  const navigate = useNavigate();
  const { setLanguage } = useApp();

  const handleLanguageSelect = (langCode: string) => {
    setLanguage(langCode);
    navigate('/home');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 flex flex-col">
      {/* Header */}
      <div className="bg-white shadow-sm p-6">
        <div className="flex items-center justify-center gap-3">
          <div className="w-12 h-12 bg-gradient-to-br from-blue-600 to-green-600 rounded-2xl flex items-center justify-center">
            <Globe className="w-7 h-7 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-2xl text-gray-900">Sahayak</h1>
            <p className="text-sm text-gray-600">सहायक • Healthcare for All</p>
          </div>
        </div>
      </div>

      {/* Language Selection */}
      <div className="flex-1 p-6 overflow-y-auto">
        <div className="max-w-md mx-auto">
          <h2 className="text-xl font-semibold text-gray-900 mb-2 text-center">
            Select Your Language
          </h2>
          <p className="text-gray-600 text-center mb-6">
            अपनी भाषा चुनें • Choose Your Language
          </p>

          <div className="space-y-3">
            {languages.map((lang) => (
              <Card
                key={lang.code}
                className="p-4 hover:shadow-md transition-all cursor-pointer border-2 border-transparent hover:border-blue-500"
                onClick={() => handleLanguageSelect(lang.code)}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">{lang.nativeName}</p>
                    <p className="text-sm text-gray-600">{lang.name}</p>
                  </div>
                  <ChevronRight className="w-5 h-5 text-gray-400" />
                </div>
              </Card>
            ))}
          </div>

          <div className="mt-8 bg-blue-50 rounded-xl p-4">
            <p className="text-sm text-gray-700 text-center">
              Powered by PM-JAY & Jan Aushadhi Scheme
            </p>
            <p className="text-xs text-gray-600 text-center mt-1">
              Ayushman Bharat Digital Mission
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
