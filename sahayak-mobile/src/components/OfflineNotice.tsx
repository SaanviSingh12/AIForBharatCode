/**
 * Offline Notice Component
 * Displays connectivity status banner for rural users
 */

import React from 'react';
import { View, Text, TouchableOpacity, Animated } from 'react-native';
import { useNetworkStatus } from '../hooks/useNetworkStatus';

interface OfflineNoticeProps {
  /** Custom message for offline state */
  offlineMessage?: string;
  /** Custom message for slow connection */
  slowMessage?: string;
  /** Show retry button */
  showRetry?: boolean;
  /** Callback when retry is pressed */
  onRetry?: () => void;
}

export function OfflineNotice({
  offlineMessage = 'No internet connection. Some features may not work.',
  slowMessage = 'Slow connection detected. Please be patient.',
  showRetry = true,
  onRetry,
}: OfflineNoticeProps) {
  const { isOffline, isSlowNetwork, refresh } = useNetworkStatus();

  const handleRetry = async () => {
    await refresh();
    onRetry?.();
  };

  if (!isOffline && !isSlowNetwork) {
    return null;
  }

  return (
    <View
      className={`px-4 py-3 flex-row items-center justify-between ${
        isOffline ? 'bg-red-600' : 'bg-yellow-500'
      }`}
    >
      <View className="flex-1 mr-2">
        <Text className={`font-medium ${isOffline ? 'text-white' : 'text-yellow-900'}`}>
          {isOffline ? '📡 ' : '⚠️ '}
          {isOffline ? offlineMessage : slowMessage}
        </Text>
      </View>
      
      {showRetry && (
        <TouchableOpacity
          onPress={handleRetry}
          className={`px-3 py-1 rounded ${
            isOffline ? 'bg-red-700' : 'bg-yellow-600'
          }`}
          activeOpacity={0.7}
        >
          <Text className={`font-medium ${isOffline ? 'text-white' : 'text-yellow-900'}`}>
            Retry
          </Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

/**
 * Localized offline messages for supported languages
 */
export const offlineMessages: Record<string, { offline: string; slow: string }> = {
  'hi-IN': {
    offline: 'इंटरनेट कनेक्शन नहीं है। कुछ सुविधाएं काम नहीं कर सकती हैं।',
    slow: 'धीमा कनेक्शन। कृपया धैर्य रखें।',
  },
  'ta-IN': {
    offline: 'இணைய இணைப்பு இல்லை. சில அம்சங்கள் வேலை செய்யாமல் போகலாம்.',
    slow: 'மெதுவான இணைப்பு. பொறுமையாக இருங்கள்.',
  },
  'te-IN': {
    offline: 'ఇంటర్నెట్ కనెక్షన్ లేదు. కొన్ని ఫీచర్లు పనిచేయకపోవచ్చు.',
    slow: 'నెమ్మదిగా కనెక్షన్. దయచేసి ఓపికగా ఉండండి.',
  },
  'kn-IN': {
    offline: 'ಇಂಟರ್ನೆಟ್ ಸಂಪರ್ಕವಿಲ್ಲ. ಕೆಲವು ವೈಶಿಷ್ಟ್ಯಗಳು ಕಾರ್ಯನಿರ್ವಹಿಸದಿರಬಹುದು.',
    slow: 'ನಿಧಾನ ಸಂಪರ್ಕ. ದಯವಿಟ್ಟು ತಾಳ್ಮೆಯಿಂದಿರಿ.',
  },
  'mr-IN': {
    offline: 'इंटरनेट कनेक्शन नाही. काही वैशिष्ट्ये कार्य करणार नाहीत.',
    slow: 'धीमे कनेक्शन. कृपया धीर धरा.',
  },
  'bn-IN': {
    offline: 'ইন্টারনেট সংযোগ নেই। কিছু বৈশিষ্ট্য কাজ নাও করতে পারে।',
    slow: 'ধীর সংযোগ। দয়া করে ধৈর্য ধরুন।',
  },
  'gu-IN': {
    offline: 'ઇન્ટરનેટ કનેક્શન નથી. કેટલીક સુવિધાઓ કામ ન કરી શકે.',
    slow: 'ધીમું કનેક્શન. કૃપા કરીને ધીરજ રાખો.',
  },
  'ml-IN': {
    offline: 'ഇന്റർനെറ്റ് കണക്ഷൻ ഇല്ല. ചില സവിശേഷതകൾ പ്രവർത്തിക്കാതെ പോകാം.',
    slow: 'സ്ലോ കണക്ഷൻ. ക്ഷമയോടെ കാത്തിരിക്കുക.',
  },
  'pa-IN': {
    offline: 'ਇੰਟਰਨੈਟ ਕੁਨੈਕਸ਼ਨ ਨਹੀਂ ਹੈ। ਕੁਝ ਵਿਸ਼ੇਸ਼ਤਾਵਾਂ ਕੰਮ ਨਹੀਂ ਕਰ ਸਕਦੀਆਂ।',
    slow: 'ਹੌਲੀ ਕੁਨੈਕਸ਼ਨ। ਕਿਰਪਾ ਕਰਕੇ ਧੀਰਜ ਰੱਖੋ।',
  },
  'en-IN': {
    offline: 'No internet connection. Some features may not work.',
    slow: 'Slow connection detected. Please be patient.',
  },
};

/**
 * Localized Offline Notice Component
 */
interface LocalizedOfflineNoticeProps extends Omit<OfflineNoticeProps, 'offlineMessage' | 'slowMessage'> {
  language?: string;
}

export function LocalizedOfflineNotice({ 
  language = 'en-IN', 
  ...props 
}: LocalizedOfflineNoticeProps) {
  const messages = offlineMessages[language] || offlineMessages['en-IN'];
  
  return (
    <OfflineNotice
      offlineMessage={messages.offline}
      slowMessage={messages.slow}
      {...props}
    />
  );
}

export default OfflineNotice;
