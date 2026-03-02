import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router';
import { Mic, MicOff, Send, ArrowLeft, AlertTriangle, Volume2 } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { useTranslation } from '../i18n';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Textarea } from '../components/ui/textarea';
import { BottomNav } from '../components/BottomNav';
import { analyzeSymptoms as apiAnalyzeSymptoms, playAudioResponse } from '../services/api';

export const SymptomEntry: React.FC = () => {
  const navigate = useNavigate();
  const { language, setSymptoms, setTriageResult, setIsLoading, setApiError } = useApp();
  const t = useTranslation();

  const [isRecording, setIsRecording] = useState(false);
  const [symptomText, setSymptomText] = useState('');
  const [aiResponse, setAiResponse] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [audioBase64, setAudioBase64] = useState<string | null>(null);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);

  const handleRecordToggle = async () => {
    if (isRecording) {
      // Stop recording
      mediaRecorderRef.current?.stop();
      setIsRecording(false);
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });
        mediaRecorderRef.current = mediaRecorder;
        audioChunksRef.current = [];

        mediaRecorder.ondataavailable = (e) => {
          if (e.data.size > 0) audioChunksRef.current.push(e.data);
        };

        mediaRecorder.onstop = async () => {
          stream.getTracks().forEach((t) => t.stop());
          const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
          await runAnalysis(audioBlob, null);
        };

        mediaRecorder.start();
        setIsRecording(true);

        // Auto-stop after 10 seconds
        setTimeout(() => {
          if (mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
            setIsRecording(false);
          }
        }, 10000);
      } catch {
        // Microphone not available — fall back to text mode
        setSymptomText('I have a headache and mild fever');
        setIsRecording(false);
      }
    }
  };

  const runAnalysis = async (audioBlob: Blob | null, text: string | null) => {
    const input = text ?? symptomText;
    setIsAnalyzing(true);
    setIsLoading(true);
    setApiError(null);

    if (input) setSymptoms(input);

    try {
      const result = await apiAnalyzeSymptoms(
        audioBlob,
        language,
        {},
        text ?? (input || undefined)
      );

      setTriageResult(result);

      if (result.success) {
        setAiResponse(result.responseText || result.summary || '');
        if (result.audioBase64) setAudioBase64(result.audioBase64);

        if (result.isEmergency) {
          navigate('/emergency');
        } else {
          // Stay on page so user can read the response, then go to doctors
        }
      } else {
        setApiError(result.error || 'Analysis failed');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Network error';
      setApiError(msg);
      // Fallback: check emergency keywords client-side
      const emergencyKeywords = ['chest pain', 'difficulty breathing', 'unconscious', 'severe bleeding', 'heart attack', 'stroke'];
      const isEmergency = emergencyKeywords.some((kw) => input.toLowerCase().includes(kw));
      if (isEmergency) navigate('/emergency');
      else setAiResponse('Unable to connect to AI service. Please check your symptoms and consult a doctor if needed.');
    } finally {
      setIsAnalyzing(false);
      setIsLoading(false);
    }
  };

  const handleSubmit = () => {
    if (symptomText.trim()) {
      runAnalysis(null, symptomText);
    }
  };

  const handleFindDoctors = () => {
    navigate('/doctor-search', { state: { fromSymptoms: true } });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
      {/* Header */}
      <div className="bg-white shadow-sm p-4 flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate('/home')}>
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h1 className="font-semibold text-lg">{t.symptom_title}</h1>
      </div>

      <div className="p-6 space-y-6">
        {/* Voice Input Section */}
        <Card className="p-6">
          <div className="flex flex-col items-center">
            <p className="text-gray-700 mb-4 text-center">{t.symptom_speak}</p>
            <button
              onClick={handleRecordToggle}
              className={`w-20 h-20 rounded-full flex items-center justify-center transition-all ${
                isRecording
                  ? 'bg-red-500 animate-pulse'
                  : 'bg-gradient-to-br from-blue-500 to-green-500'
              }`}
            >
              {isRecording ? (
                <MicOff className="w-10 h-10 text-white" />
              ) : (
                <Mic className="w-10 h-10 text-white" />
              )}
            </button>
            {isRecording && (
              <p className="text-red-500 mt-3 animate-pulse">{t.symptom_recording}</p>
            )}
          </div>
        </Card>

        {/* Text Input Section */}
        <Card className="p-6">
          <p className="text-gray-700 mb-3">{t.symptom_or_type}</p>
          <Textarea
            placeholder={t.symptom_type_placeholder}
            value={symptomText}
            onChange={(e) => setSymptomText(e.target.value)}
            className="min-h-32 mb-3"
          />
          <Button
            onClick={handleSubmit}
            className="w-full bg-gradient-to-r from-blue-600 to-green-600"
            disabled={!symptomText.trim() || isAnalyzing}
          >
            {isAnalyzing ? (
              <span>{t.symptom_analyzing}</span>
            ) : (
              <>
                <Send className="w-4 h-4 mr-2" />
                {t.symptom_analyze}
              </>
            )}
          </Button>
        </Card>

        {/* AI Response */}
        {aiResponse && (
          <Card className="p-6 bg-blue-50 border-blue-200">
            <div className="flex items-start gap-3 mb-4">
              <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0">
                <AlertTriangle className="w-5 h-5 text-white" />
              </div>
              <div className="flex-1">
                <h3 className="font-semibold text-blue-900 mb-2">{t.symptom_ai_analysis}</h3>
                <p className="text-blue-800 text-sm">{aiResponse}</p>
              </div>
            </div>

            {audioBase64 && (
              <Button
                variant="outline"
                size="sm"
                className="mb-3"
                onClick={() => playAudioResponse(audioBase64)}
              >
                <Volume2 className="w-4 h-4 mr-2" />
                {t.symptom_play_audio}
              </Button>
            )}

            <Button
              onClick={handleFindDoctors}
              className="w-full mt-2 bg-blue-600 hover:bg-blue-700"
            >
              {t.symptom_find_doctors}
            </Button>
          </Card>
        )}

        {/* Emergency Warning */}
        <Card className="p-4 bg-yellow-50 border-yellow-200">
          <div className="flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-yellow-800">
              {t.symptom_emergency_warning}{' '}
              <span className="font-bold">108</span>
            </p>
          </div>
        </Card>
      </div>

      <BottomNav />
    </div>
  );
};
