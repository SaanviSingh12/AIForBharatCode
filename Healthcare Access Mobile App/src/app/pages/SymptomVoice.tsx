import { ArrowLeft, Mic, MicOff } from 'lucide-react';
import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { Button } from '../components/ui/button';
import { useApp } from '../context/AppContext';

export const SymptomVoice: React.FC = () => {
    const navigate = useNavigate();
    const { language, setPendingAudioBlob } = useApp();
    const t = getTranslations(language);

    const [isRecording, setIsRecording] = useState(false);
    const [seconds, setSeconds] = useState(0);
    const [hasRecorded, setHasRecorded] = useState(false);

    const mediaRecorderRef = useRef<MediaRecorder | null>(null);
    const audioChunksRef = useRef<Blob[]>([]);
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

    useEffect(() => {
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
            mediaRecorderRef.current?.stream?.getTracks().forEach((t) => t.stop());
        };
    }, []);

    const startRecording = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            const mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });
            mediaRecorderRef.current = mediaRecorder;
            audioChunksRef.current = [];

            mediaRecorder.ondataavailable = (e) => {
                if (e.data.size > 0) audioChunksRef.current.push(e.data);
            };

            mediaRecorder.onstop = () => {
                stream.getTracks().forEach((t) => t.stop());
                if (timerRef.current) clearInterval(timerRef.current);
                setHasRecorded(true);

                const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
                setPendingAudioBlob(audioBlob);
                navigate('/symptom-analysis');
            };

            mediaRecorder.start();
            setIsRecording(true);
            setSeconds(0);

            timerRef.current = setInterval(() => {
                setSeconds((s) => s + 1);
            }, 1000);

            // Auto-stop after 30 seconds
            setTimeout(() => {
                if (mediaRecorder.state === 'recording') {
                    mediaRecorder.stop();
                    setIsRecording(false);
                }
            }, 30000);
        } catch {
            // Microphone unavailable — go back to symptom entry
            navigate('/symptom-entry');
        }
    };

    const stopRecording = () => {
        if (mediaRecorderRef.current?.state === 'recording') {
            mediaRecorderRef.current.stop();
            setIsRecording(false);
        }
    };

    const formatTime = (s: number) => {
        const mins = Math.floor(s / 60);
        const secs = s % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-indigo-900 via-purple-900 to-blue-900 flex flex-col">
            {/* Header */}
            <div className="p-4 flex items-center gap-3">
                <Button
                    variant="ghost"
                    size="icon"
                    className="text-white hover:bg-white/10"
                    onClick={() => {
                        if (isRecording) stopRecording();
                        navigate('/symptom-entry');
                    }}
                >
                    <ArrowLeft className="w-5 h-5" />
                </Button>
                <h1 className="font-semibold text-lg text-white">{t.speakSymptoms}</h1>
            </div>

            {/* Center content */}
            <div className="flex-1 flex flex-col items-center justify-center px-6">
                {!isRecording && !hasRecorded && (
                    <>
                        <p className="text-white/70 text-center mb-8 text-lg">
                            Tap the microphone and describe your symptoms
                        </p>

                        {/* Mic button */}
                        <button
                            onClick={startRecording}
                            className="w-32 h-32 rounded-full bg-gradient-to-br from-blue-500 to-green-500 flex items-center justify-center shadow-2xl shadow-blue-500/30 transition-transform hover:scale-105 active:scale-95"
                        >
                            <Mic className="w-14 h-14 text-white" />
                        </button>

                        <p className="text-white/50 text-sm mt-6">Up to 30 seconds</p>
                    </>
                )}

                {isRecording && (
                    <>
                        {/* Animated rings */}
                        <div className="relative flex items-center justify-center mb-8">
                            <div className="absolute w-48 h-48 rounded-full border-2 border-red-400/30 animate-ping" />
                            <div className="absolute w-40 h-40 rounded-full border-2 border-red-400/20 animate-pulse" />
                            <button
                                onClick={stopRecording}
                                className="relative w-32 h-32 rounded-full bg-red-500 flex items-center justify-center shadow-2xl shadow-red-500/40 transition-transform active:scale-95 z-10"
                            >
                                <MicOff className="w-14 h-14 text-white" />
                            </button>
                        </div>

                        <p className="text-red-300 text-xl font-semibold animate-pulse">
                            {t.recording || 'Recording...'}
                        </p>
                        <p className="text-white/80 text-3xl font-mono mt-3">{formatTime(seconds)}</p>
                        <p className="text-white/40 text-sm mt-4">Tap to stop</p>
                    </>
                )}
            </div>
        </div>
    );
};
