import { AlertTriangle, CheckCircle2, Loader2, Mic, Search, Stethoscope, Volume2 } from 'lucide-react';
import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { getTranslations } from '../../i18n';
import { Button } from '../components/ui/button';
import { useApp } from '../context/AppContext';
import { analyzeSymptoms as apiAnalyzeSymptoms, playAudioResponse } from '../services/api';

type Step = {
    label: string;
    icon: React.ReactNode;
    status: 'pending' | 'active' | 'done' | 'error';
};

export const SymptomAnalysis: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { language, setSymptoms, setTriageResult, setIsLoading, setApiError, pendingAudioBlob, setPendingAudioBlob, userLocation } = useApp();
    const t = getTranslations(language);

    const audioBlob: Blob | null = pendingAudioBlob;
    const directText: string | null = (location.state as any)?.directText ?? null;

    const [steps, setSteps] = useState<Step[]>([
        { label: 'Receiving your input', icon: <Mic className="w-5 h-5" />, status: 'pending' },
        { label: 'Analyzing symptoms with AI', icon: <Search className="w-5 h-5" />, status: 'pending' },
        { label: 'Finding specialist & hospitals', icon: <Stethoscope className="w-5 h-5" />, status: 'pending' },
    ]);
    const [audioBase64, setAudioBase64] = useState<string | null>(null);
    const [summary, setSummary] = useState<string | null>(null);
    const [failed, setFailed] = useState(false);
    const [showUserInput, setShowUserInput] = useState(true);
    const [hasAudioInput] = useState<boolean>(!!audioBlob);
    const [showResponseText, setShowResponseText] = useState(false);

    const hasRun = useRef(false);
    const mockResponseTextRef = useRef<string | null>(null);
    const mockAudioRef = useRef<string | null>(null);

    const updateStep = (index: number, status: Step['status']) => {
        setSteps((prev) => prev.map((s, i) => (i === index ? { ...s, status } : s)));
    };

    // Hide user input after 5 seconds
    useEffect(() => {
        const timer = setTimeout(() => {
            setShowUserInput(false);
        }, 5000);

        return () => clearTimeout(timer);
    }, []);

    // Hide response text after 5 seconds
    useEffect(() => {
        if (summary) {
            setShowResponseText(true);
            const timer = setTimeout(() => {
                setShowResponseText(false);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [summary]);

    useEffect(() => {
        if (hasRun.current) return;
        hasRun.current = true;

        if (!audioBlob && !directText) {
            navigate('/symptom-entry', { replace: true });
            return;
        }

        // Clear the pending blob from context after capturing it
        if (audioBlob) setPendingAudioBlob(null);

        runAnalysis();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const runAnalysis = async () => {
        setIsLoading(true);
        setApiError(null);
        setTriageResult(null);

        if (directText) setSymptoms(directText);

        // Step 1: Input received
        updateStep(0, 'active');
        await delay(600);
        updateStep(0, 'done');

        // Step 2: Analyzing
        updateStep(1, 'active');

        // DEMO: Show mock response text and audio at 1.5 seconds (before API completes)
        const mockResponseText = 'आपके लक्षणों के आधार पर, मैं एक सामान्य चिकित्सक से परामर्श करने की सलाह देता हूं। आपके लक्षण हल्के श्वसन संक्रमण का संकेत देते हैं। कृपया जल्द ही एक अपॉइंटमेंट शेड्यूल करें।';
        // Mock audio base64 (minimal MP3 file simulating AWS Polly response)
        // NOTE: This is a very short silent MP3 for demo purposes.
        // To test with real audio, see MOCK_AUDIO_GUIDE.md in the project root
        // Replace this with a real base64-encoded MP3 for actual audio playback
        const mockAudio = 'SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWGluZwAAAA8AAAACAAADhAC7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7v////////////////////////////////////////////////////////////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//sQZAAP8AAAaQAAAAgAAA0gAAABAAABpAAAACAAADSAAAAETEFN//sQZDIP8AAAaQAAAAgAAA0gAAABAAABpAAAACAAADSAAAAEUzLjEwMFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVQ==';

        // Store in refs so they're immediately accessible
        mockResponseTextRef.current = mockResponseText;
        mockAudioRef.current = mockAudio;

        setTimeout(() => {
            setSummary(mockResponseText);
            setAudioBase64(mockAudio);
        }, 1500);

        try {
            // Run API call and minimum display time in parallel
            const [result] = await Promise.all([
                apiAnalyzeSymptoms(
                    audioBlob,
                    language,
                    userLocation ?? {},
                    directText ?? undefined
                ),
                delay(2000), // ensure step 2 shows for at least 2 seconds
            ]);

            setTriageResult(result);
            updateStep(1, 'done');

            if (result.success) {
                setSummary(result.responseText || result.summary || '');
                if (result.audioBase64) setAudioBase64(result.audioBase64);

                // Step 3: Finding specialists
                updateStep(2, 'active');
                await delay(800);
                updateStep(2, 'done');

                // Brief pause to let user see everything completed
                await delay(1200);

                // Use the mock values from refs (set immediately) or API response
                const responseTextToPass = result.responseText || result.summary || mockResponseTextRef.current || '';
                const audioToPass = result.audioBase64 || mockAudioRef.current || null;

                console.log('Navigating with:', { responseTextToPass, audioToPass: audioToPass ? 'present' : 'null' });

                if (result.isEmergency) {
                    navigate('/emergency', { replace: true, state: { responseText: responseTextToPass, audioBase64: audioToPass } });
                } else {
                    navigate('/doctor-search', { replace: true, state: { fromSymptoms: true, responseText: responseTextToPass, audioBase64: audioToPass } });
                }
            } else {
                updateStep(1, 'error');
                setApiError(result.error || 'Analysis failed');
                setFailed(true);
            }
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Network error';
            updateStep(1, 'error');
            setApiError(msg);

            // Fallback: check emergency keywords client-side
            if (directText) {
                const emergencyKeywords = ['chest pain', 'difficulty breathing', 'unconscious', 'severe bleeding', 'heart attack', 'stroke'];
                const isEmergency = emergencyKeywords.some((kw) => directText.toLowerCase().includes(kw));
                if (isEmergency) {
                    await delay(800);
                    navigate('/emergency', { replace: true });
                    return;
                }
            }
            setFailed(true);
        } finally {
            setIsLoading(false);
        }
    };

    const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

    const stepIcon = (step: Step) => {
        if (step.status === 'active') return <Loader2 className="w-5 h-5 animate-spin text-blue-500" />;
        if (step.status === 'done') return <CheckCircle2 className="w-5 h-5 text-green-500" />;
        if (step.status === 'error') return <AlertTriangle className="w-5 h-5 text-red-500" />;
        return <span className="w-5 h-5 text-gray-300">{step.icon}</span>;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 flex flex-col">
            {/* Header */}
            <div className="bg-white shadow-sm p-4">
                <h1 className="font-semibold text-lg text-center">{t.analyzing || 'Analyzing...'}</h1>
            </div>

            <div className="flex-1 flex flex-col items-center justify-center px-6 py-10">
                {/* User Input Display (shown for 5 seconds) */}
                {showUserInput && (directText || hasAudioInput) && (
                    <div className="w-full max-w-sm bg-gradient-to-r from-blue-100 to-green-100 rounded-xl border border-blue-200 p-4 mb-6 shadow-md animate-fade-in">
                        <div className="flex items-start gap-3">
                            <div className="bg-white rounded-full p-2 mt-0.5">
                                {hasAudioInput ? (
                                    <Mic className="w-4 h-4 text-blue-600" />
                                ) : (
                                    <Search className="w-4 h-4 text-blue-600" />
                                )}
                            </div>
                            <div className="flex-1">
                                <p className="text-xs font-medium text-gray-600 mb-1">Input:</p>
                                <p className="text-sm text-gray-800 font-medium">
                                    {directText || 'Voice recording received'}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* Steps list */}
                <div className="w-full max-w-sm space-y-4 mb-8">
                    {steps.map((step, i) => (
                        <div
                            key={i}
                            className={`flex items-center gap-4 p-4 rounded-xl transition-all duration-500 ${step.status === 'active'
                                ? 'bg-blue-50 border border-blue-200 shadow-sm'
                                : step.status === 'done'
                                    ? 'bg-green-50 border border-green-200'
                                    : step.status === 'error'
                                        ? 'bg-red-50 border border-red-200'
                                        : 'bg-white border border-gray-100'
                                }`}
                        >
                            {stepIcon(step)}
                            <span
                                className={`text-sm font-medium ${step.status === 'active'
                                    ? 'text-blue-700'
                                    : step.status === 'done'
                                        ? 'text-green-700'
                                        : step.status === 'error'
                                            ? 'text-red-700'
                                            : 'text-gray-400'
                                    }`}
                            >
                                {step.label}
                            </span>
                        </div>
                    ))}
                </div>

                {/* Response Text (shown for 5 seconds) */}
                {summary && showResponseText && (
                    <div className="w-full max-w-sm bg-gradient-to-r from-green-100 to-blue-100 rounded-xl border border-green-200 p-4 mb-6 shadow-md animate-fade-in">
                        <div className="flex items-start gap-3">
                            <div className="bg-white rounded-full p-2 mt-0.5">
                                <Stethoscope className="w-4 h-4 text-green-600" />
                            </div>
                            <div className="flex-1">
                                <p className="text-xs font-medium text-gray-600 mb-1">Analysis Result:</p>
                                <p className="text-sm text-gray-800 font-medium">{summary}</p>
                                {audioBase64 && (
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="mt-3"
                                        onClick={() => playAudioResponse(audioBase64)}
                                    >
                                        <Volume2 className="w-4 h-4 mr-2" />
                                        Play Audio Response
                                    </Button>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {/* Error state */}
                {failed && (
                    <div className="w-full max-w-sm space-y-3">
                        <p className="text-sm text-red-600 text-center">
                            Unable to complete analysis. Please try again.
                        </p>
                        <Button
                            onClick={() => navigate('/symptom-entry', { replace: true })}
                            className="w-full bg-gradient-to-r from-blue-600 to-green-600"
                        >
                            {t.retry || 'Retry'}
                        </Button>
                    </div>
                )}
            </div>
        </div>
    );
};
