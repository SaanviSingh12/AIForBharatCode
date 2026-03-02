import { AlertCircle, Heart, Pill, Stethoscope, User } from 'lucide-react';
import React from 'react';
import { useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { BottomNav } from '../components/BottomNav';
import { Card } from '../components/ui/card';
import { useApp } from '../context/AppContext';

export const Home: React.FC = () => {
    const navigate = useNavigate();
    const { language, userProfile } = useApp();
    const t = getTranslations(language);

    const menuItems = [
        {
            title: t.symptomEntry,
            subtitle: t.aiPoweredAnalysis,
            icon: Heart,
            color: 'from-red-500 to-pink-500',
            path: '/symptom-entry',
        },
        {
            title: t.findDoctor,
            subtitle: t.govtPrivateDoctors,
            icon: Stethoscope,
            color: 'from-blue-500 to-cyan-500',
            path: '/doctor-search',
        },
        {
            title: t.prescription,
            subtitle: t.genericMedicines,
            icon: Pill,
            color: 'from-green-500 to-emerald-500',
            path: '/prescription-search',
        },
        {
            title: t.profile,
            subtitle: t.yourDetails,
            icon: User,
            color: 'from-purple-500 to-indigo-500',
            path: '/profile',
        },
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
            {/* Header */}
            <div className="bg-gradient-to-r from-blue-600 to-green-600 text-white p-6 rounded-b-3xl shadow-lg">
                <div className="flex items-center justify-between mb-4">
                    <div>
                        <h1 className="font-bold text-2xl">Sahayak</h1>
                        <p className="text-blue-100 text-sm">{t.healthcareForAll}</p>
                    </div>
                    <div
                        className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center cursor-pointer hover:bg-white/30"
                        onClick={() => navigate('/profile')}
                    >
                        <User className="w-6 h-6" />
                    </div>
                </div>
                <div className="bg-white/10 backdrop-blur-sm rounded-xl p-4">
                    <p className="text-sm text-blue-100">{t.welcome}</p>
                    <p className="font-semibold text-lg">{userProfile.name}</p>
                </div>
            </div>

            {/* Emergency Alert */}
            <div className="px-6 mt-6">
                <div
                    className="bg-red-50 border-2 border-red-200 rounded-xl p-4 flex items-center gap-3 cursor-pointer hover:bg-red-100 transition-all"
                    onClick={() => navigate('/emergency')}
                >
                    <div className="w-10 h-10 bg-red-500 rounded-full flex items-center justify-center">
                        <AlertCircle className="w-6 h-6 text-white" />
                    </div>
                    <div className="flex-1">
                        <p className="font-semibold text-red-900">{t.emergency}</p>
                        <p className="text-sm text-red-700">{t.tapForEmergency}</p>
                    </div>
                </div>
            </div>

            {/* Main Menu */}
            <div className="px-6 mt-6">
                <h2 className="font-semibold text-gray-900 mb-4">{t.services}</h2>
                <div className="grid grid-cols-2 gap-4">
                    {menuItems.map((item, index) => (
                        <Card
                            key={index}
                            className="p-6 cursor-pointer hover:shadow-xl transition-all transform hover:-translate-y-1"
                            onClick={() => navigate(item.path)}
                        >
                            <div className={`w-14 h-14 bg-gradient-to-br ${item.color} rounded-2xl flex items-center justify-center mb-4`}>
                                <item.icon className="w-7 h-7 text-white" />
                            </div>
                            <h3 className="font-semibold text-gray-900 mb-1">{item.title}</h3>
                            <p className="text-xs text-gray-600">{item.subtitle}</p>
                        </Card>
                    ))}
                </div>
            </div>

            {/* Government Schemes */}
            <div className="px-6 mt-6">
                <h2 className="font-semibold text-gray-900 mb-4">{t.governmentSchemes}</h2>
                <div className="space-y-3">
                    <Card className="p-4 bg-gradient-to-r from-orange-50 to-orange-100">
                        <h3 className="font-semibold text-orange-900 mb-1">{t.pmjayTitle}</h3>
                        <p className="text-sm text-orange-800">
                            {t.pmjayDescription}
                        </p>
                    </Card>
                    <Card className="p-4 bg-gradient-to-r from-green-50 to-green-100">
                        <h3 className="font-semibold text-green-900 mb-1">{t.janAushadhiTitle}</h3>
                        <p className="text-sm text-green-800">
                            {t.janAushadhiDescription}
                        </p>
                    </Card>
                </div>
            </div>

            <BottomNav />
        </div>
    );
};