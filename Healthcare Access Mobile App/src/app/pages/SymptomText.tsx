import { ArrowLeft, Send } from 'lucide-react';
import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import { Textarea } from '../components/ui/textarea';
import { useApp } from '../context/AppContext';

export const SymptomText: React.FC = () => {
    const navigate = useNavigate();
    const { language } = useApp();
    const t = getTranslations(language);

    const [symptomText, setSymptomText] = useState('');

    const handleSubmit = () => {
        if (symptomText.trim()) {
            navigate('/symptom-analysis', { state: { directText: symptomText } });
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 flex flex-col">
            {/* Header */}
            <div className="bg-white shadow-sm p-4 flex items-center gap-3">
                <Button variant="ghost" size="icon" onClick={() => navigate('/symptom-entry')}>
                    <ArrowLeft className="w-5 h-5" />
                </Button>
                <h1 className="font-semibold text-lg">{t.typeYourSymptoms}</h1>
            </div>

            <div className="flex-1 flex flex-col p-6">
                <Card className="flex-1 p-6 flex flex-col">
                    <p className="text-gray-700 mb-3">Describe what you're feeling:</p>
                    <Textarea
                        placeholder={t.typeSymptoms}
                        value={symptomText}
                        onChange={(e) => setSymptomText(e.target.value)}
                        className="flex-1 min-h-40 mb-4 resize-none"
                        autoFocus
                    />
                    <Button
                        onClick={handleSubmit}
                        className="w-full bg-gradient-to-r from-blue-600 to-green-600"
                        disabled={!symptomText.trim()}
                    >
                        <Send className="w-4 h-4 mr-2" />
                        Analyze Symptoms
                    </Button>
                </Card>
            </div>
        </div>
    );
};
