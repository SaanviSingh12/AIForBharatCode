// Language capability indicators for UI
// Shows which features are available for each language

import { FileText, Mic, Volume2 } from 'lucide-react';
import React from 'react';
import { isVoiceInputSupported, isVoiceOutputSupported } from '../../i18n';

interface LanguageCapabilityBadgesProps {
    languageCode: string;
    showLabels?: boolean;
}

/**
 * Display badges showing which capabilities are available for a language
 * Usage: <LanguageCapabilityBadges languageCode="hi" showLabels={true} />
 */
export const LanguageCapabilityBadges: React.FC<LanguageCapabilityBadgesProps> = ({
    languageCode,
    showLabels = false
}) => {
    const hasVoiceInput = isVoiceInputSupported(languageCode);
    const hasVoiceOutput = isVoiceOutputSupported(languageCode);

    return (
        <div className="flex items-center gap-2">
            {/* Voice Input */}
            <div className={`flex items-center gap-1 ${hasVoiceInput ? 'text-green-600' : 'text-gray-300'}`}>
                <Mic className="w-4 h-4" />
                {showLabels && <span className="text-xs">Voice In</span>}
            </div>

            {/* Voice Output */}
            <div className={`flex items-center gap-1 ${hasVoiceOutput ? 'text-blue-600' : 'text-gray-300'}`}>
                <Volume2 className="w-4 h-4" />
                {showLabels && <span className="text-xs">Voice Out</span>}
            </div>

            {/* Text is always available */}
            <div className="flex items-center gap-1 text-purple-600">
                <FileText className="w-4 h-4" />
                {showLabels && <span className="text-xs">Text</span>}
            </div>
        </div>
    );
};

interface LanguageCapabilityMessageProps {
    languageCode: string;
}

/**
 * Display a helpful message about language capabilities
 * Shows what features work and what don't
 */
export const LanguageCapabilityMessage: React.FC<LanguageCapabilityMessageProps> = ({
    languageCode
}) => {
    const hasVoiceInput = isVoiceInputSupported(languageCode);
    const hasVoiceOutput = isVoiceOutputSupported(languageCode);

    if (hasVoiceInput && hasVoiceOutput) {
        return (
            <div className="bg-green-50 border border-green-200 rounded-lg p-3 text-sm">
                <p className="text-green-800">
                    ✅ Full voice support available - You can speak and hear responses
                </p>
            </div>
        );
    }

    if (hasVoiceInput && !hasVoiceOutput) {
        return (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-sm">
                <p className="text-yellow-800">
                    🎤 Voice input available - You can speak, but responses will be text only
                </p>
            </div>
        );
    }

    if (!hasVoiceInput && !hasVoiceOutput) {
        return (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
                <p className="text-blue-800">
                    ✍️ Text-only mode - Please type your symptoms and read responses
                </p>
            </div>
        );
    }

    return null;
};
