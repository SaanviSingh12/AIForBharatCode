import React from 'react';
import { useNavigate } from 'react-router';
import { ArrowLeft, Phone, AlertTriangle, Ambulance, Heart, Shield, Flame, Users, Baby } from 'lucide-react';
import { emergencyNumbers } from '../data/mockData';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';

export const EmergencyMode: React.FC = () => {
  const navigate = useNavigate();

  const handleCall = (number: string) => {
    window.location.href = `tel:${number}`;
  };

  const getIcon = (iconName: string) => {
    switch (iconName) {
      case 'ambulance':
        return Ambulance;
      case 'heart-pulse':
        return Heart;
      case 'shield':
        return Shield;
      case 'flame':
        return Flame;
      case 'users':
        return Users;
      case 'baby':
        return Baby;
      default:
        return Phone;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-600 to-red-800 text-white">
      {/* Header */}
      <div className="p-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate('/home')}
          className="text-white hover:bg-white/20 mb-4"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>

        <div className="text-center mb-6">
          <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-4 animate-pulse">
            <AlertTriangle className="w-12 h-12 text-white" />
          </div>
          <h1 className="font-bold text-3xl mb-2">EMERGENCY MODE</h1>
          <p className="text-red-100">आपातकालीन मोड</p>
        </div>
      </div>

      {/* Emergency Alert */}
      <div className="px-4 mb-6">
        <Card className="bg-white/10 backdrop-blur-sm border-white/20 p-4">
          <p className="text-white text-center text-sm">
            ⚠️ If you are experiencing a life-threatening emergency, call an ambulance immediately
          </p>
        </Card>
      </div>

      {/* Emergency Contacts */}
      <div className="px-4 pb-6 space-y-3">
        <h2 className="font-semibold text-xl mb-4 text-center">Emergency Numbers</h2>
        
        {emergencyNumbers.map((emergency, index) => {
          const Icon = getIcon(emergency.icon);
          return (
            <Card
              key={index}
              className="bg-white text-gray-900 overflow-hidden hover:shadow-2xl transition-all"
            >
              <div className="flex items-center p-4">
                <div className="w-14 h-14 bg-red-100 rounded-xl flex items-center justify-center mr-4">
                  <Icon className="w-7 h-7 text-red-600" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-900 mb-1">{emergency.name}</h3>
                  <p className="text-2xl font-bold text-red-600">{emergency.number}</p>
                </div>
              </div>
              <div className="border-t border-gray-200 p-3 bg-red-50">
                <Button
                  onClick={() => handleCall(emergency.number)}
                  className="w-full bg-red-600 hover:bg-red-700 h-12 text-base"
                >
                  <Phone className="w-5 h-5 mr-2" />
                  CALL {emergency.number}
                </Button>
              </div>
            </Card>
          );
        })}
      </div>

      {/* Additional Information */}
      <div className="px-4 pb-8">
        <Card className="bg-white/10 backdrop-blur-sm border-white/20 p-4">
          <h3 className="font-semibold text-white mb-3">Important Guidelines</h3>
          <ul className="space-y-2 text-sm text-red-100">
            <li>• Stay calm and speak clearly when calling</li>
            <li>• Provide your exact location</li>
            <li>• Describe the emergency situation</li>
            <li>• Follow the operator's instructions</li>
            <li>• Don't hang up until told to do so</li>
          </ul>
        </Card>
      </div>

      {/* Safety Notice */}
      <div className="px-4 pb-6">
        <Card className="bg-yellow-500 text-yellow-900 p-4">
          <div className="flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 mt-0.5 flex-shrink-0" />
            <div>
              <p className="font-semibold mb-1">Medical Emergency Symptoms</p>
              <p className="text-sm">
                Chest pain, difficulty breathing, severe bleeding, unconsciousness, 
                stroke symptoms, severe burns, or suspected heart attack - Call 108 immediately!
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
