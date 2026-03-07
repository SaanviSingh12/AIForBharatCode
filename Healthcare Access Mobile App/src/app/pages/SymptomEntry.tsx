import { AlertTriangle, ArrowLeft, Keyboard, Mic } from 'lucide-react';
import React from 'react';
import { useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { BottomNav } from '../components/BottomNav';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { useApp } from '../context/AppContext';

export const SymptomEntry: React.FC = () => {
    const navigate = useNavigate();
    const { language } = useApp();
    const t = getTranslations(language);

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
            {/* Header */}
            <div className="bg-white shadow-sm p-4 flex items-center gap-3">
                <Button variant="ghost" size="icon" onClick={() => navigate('/home')}>
                    <ArrowLeft className="w-5 h-5" />
                </Button>
                <h1 className="font-semibold text-lg">{t.symptomEntry}</h1>
            </div>

            <div className="flex-1 flex flex-col items-center px-6 pt-12 space-y-8">
                <div className="text-center">
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">How would you like to describe your symptoms?</h2>
                    <p className="text-gray-500">Choose one of the options below</p>
                </div>

                {/* Voice option */}
                <Card
                    className="w-full p-6 cursor-pointer hover:shadow-lg transition-shadow border-2 border-transparent hover:border-blue-300"
                    onClick={() => navigate('/symptom-voice')}
                >
                    <div className="flex items-center gap-5">
                        <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-500 to-green-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                            <Mic className="w-8 h-8 text-white" />
                        </div>
                        <div className="flex-1">
                            <h3 className="font-semibold text-lg text-gray-800">{t.speakSymptoms}</h3>
                            <p className="text-sm text-gray-500 mt-1">Tap and speak in your language — AI will listen and analyze</p>
                        </div>
                    </div>
                </Card>

                {/* Text option */}
                <Card
                    className="w-full p-6 cursor-pointer hover:shadow-lg transition-shadow border-2 border-transparent hover:border-green-300"
                    onClick={() => navigate('/symptom-text')}
                >
                    <div className="flex items-center gap-5">
                        <div className="w-16 h-16 rounded-full bg-gradient-to-br from-green-500 to-teal-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                            <Keyboard className="w-8 h-8 text-white" />
                        </div>
                        <div className="flex-1">
                            <h3 className="font-semibold text-lg text-gray-800">{t.typeYourSymptoms}</h3>
                            <p className="text-sm text-gray-500 mt-1">Write down what you're feeling and let AI assist you</p>
                        </div>
                    </div>
                </Card>

                {/* Emergency Warning */}
                <Card className="w-full p-4 bg-yellow-50 border-yellow-200">
                    <div className="flex items-start gap-3">
                        <AlertTriangle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
                        <p className="text-sm text-yellow-800">
                            If you are experiencing severe or life-threatening symptoms, please call emergency services immediately at{' '}
                            <span className="font-bold">108</span>
                        </p>
                    </div>
                </Card>
            </div>

            <BottomNav />
        </div>
    );
};
