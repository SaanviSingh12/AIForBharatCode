export interface Doctor {
  id: string;
  name: string;
  specialty: string;
  type: 'government' | 'independent' | 'commercial';
  distance: number;
  phone: string;
  address: string;
  experience: number;
  languages: string[];
}

export interface Pharmacy {
  id: string;
  name: string;
  type: 'government' | 'commercial';
  distance: number;
  phone: string;
  address: string;
  timings: string;
}

export interface Medicine {
  id: string;
  genericName: string;
  brandedPrice: number;
  genericPrice: number;
  savings: number;
}

export const mockDoctors: Doctor[] = [
  {
    id: '1',
    name: 'Dr. Priya Sharma',
    specialty: 'Cardiologist',
    type: 'government',
    distance: 1.2,
    phone: '+91-9876543210',
    address: 'Government District Hospital, Sector 10',
    experience: 15,
    languages: ['Hindi', 'English', 'Punjabi'],
  },
  {
    id: '2',
    name: 'Dr. Amit Patel',
    specialty: 'General Physician',
    type: 'government',
    distance: 2.5,
    phone: '+91-9876543211',
    address: 'Primary Health Centre, Nehru Nagar',
    experience: 12,
    languages: ['Hindi', 'English', 'Gujarati'],
  },
  {
    id: '3',
    name: 'Dr. Sunita Reddy',
    specialty: 'Pediatrician',
    type: 'independent',
    distance: 3.1,
    phone: '+91-9876543212',
    address: 'Reddy Clinic, Gandhi Road',
    experience: 10,
    languages: ['Hindi', 'English', 'Telugu'],
  },
  {
    id: '4',
    name: 'Dr. Rajesh Kumar',
    specialty: 'Nephrologist',
    type: 'commercial',
    distance: 4.5,
    phone: '+91-9876543213',
    address: 'Apollo Hospital, MG Road',
    experience: 20,
    languages: ['Hindi', 'English'],
  },
  {
    id: '5',
    name: 'Dr. Anjali Verma',
    specialty: 'Dermatologist',
    type: 'government',
    distance: 1.8,
    phone: '+91-9876543214',
    address: 'Government Medical College, Civil Lines',
    experience: 8,
    languages: ['Hindi', 'English', 'Bengali'],
  },
];

export const mockPharmacies: Pharmacy[] = [
  {
    id: '1',
    name: 'Jan Aushadhi Kendra - Sector 12',
    type: 'government',
    distance: 0.8,
    phone: '+91-9876543220',
    address: 'Near District Hospital, Sector 12',
    timings: '8:00 AM - 8:00 PM',
  },
  {
    id: '2',
    name: 'Jan Aushadhi Kendra - Nehru Nagar',
    type: 'government',
    distance: 2.2,
    phone: '+91-9876543221',
    address: 'Main Market, Nehru Nagar',
    timings: '9:00 AM - 7:00 PM',
  },
  {
    id: '3',
    name: 'MedPlus Pharmacy',
    type: 'commercial',
    distance: 1.5,
    phone: '+91-9876543222',
    address: 'Gandhi Road, Near Bus Stand',
    timings: '8:00 AM - 10:00 PM',
  },
  {
    id: '4',
    name: 'Apollo Pharmacy',
    type: 'commercial',
    distance: 3.5,
    phone: '+91-9876543223',
    address: 'MG Road, Opposite Bank',
    timings: '24 Hours',
  },
  {
    id: '5',
    name: 'Jan Aushadhi Kendra - Civil Lines',
    type: 'government',
    distance: 3.8,
    phone: '+91-9876543224',
    address: 'Civil Lines, Near Post Office',
    timings: '8:00 AM - 8:00 PM',
  },
];

export const mockMedicines: Medicine[] = [
  {
    id: '1',
    genericName: 'Metformin 500mg',
    brandedPrice: 150,
    genericPrice: 25,
    savings: 83,
  },
  {
    id: '2',
    genericName: 'Amlodipine 5mg',
    brandedPrice: 200,
    genericPrice: 30,
    savings: 85,
  },
  {
    id: '3',
    genericName: 'Atorvastatin 10mg',
    brandedPrice: 180,
    genericPrice: 20,
    savings: 89,
  },
  {
    id: '4',
    genericName: 'Paracetamol 500mg',
    brandedPrice: 50,
    genericPrice: 8,
    savings: 84,
  },
];

export const emergencyNumbers = [
  {
    name: 'Ambulance (National)',
    number: '108',
    icon: 'ambulance',
  },
  {
    name: 'Medical Emergency',
    number: '102',
    icon: 'heart-pulse',
  },
  {
    name: 'Police Emergency',
    number: '100',
    icon: 'shield',
  },
  {
    name: 'Fire Emergency',
    number: '101',
    icon: 'flame',
  },
  {
    name: 'Women Helpline',
    number: '1091',
    icon: 'users',
  },
  {
    name: 'Child Helpline',
    number: '1098',
    icon: 'baby',
  },
];

export const languages = [
  { code: 'en', name: 'English', nativeName: 'English' },
  { code: 'hi', name: 'Hindi', nativeName: 'हिंदी' },
  { code: 'bn', name: 'Bengali', nativeName: 'বাংলা' },
  { code: 'te', name: 'Telugu', nativeName: 'తెలుగు' },
  { code: 'mr', name: 'Marathi', nativeName: 'मराठी' },
  { code: 'ta', name: 'Tamil', nativeName: 'தமிழ்' },
  { code: 'gu', name: 'Gujarati', nativeName: 'ગુજરાતી' },
  { code: 'kn', name: 'Kannada', nativeName: 'ಕನ್ನಡ' },
  { code: 'ml', name: 'Malayalam', nativeName: 'മലയാളം' },
  { code: 'pa', name: 'Punjabi', nativeName: 'ਪੰਜਾਬੀ' },
];

export const translations: Record<string, Record<string, string>> = {
  en: {
    home: 'Home',
    symptomEntry: 'Symptom Checker',
    findDoctor: 'Find Doctor',
    prescription: 'Prescription',
    profile: 'Profile',
    speakSymptoms: 'Speak Your Symptoms',
    typeSymptoms: 'Or type your symptoms here...',
    analyzing: 'Analyzing...',
    findDoctors: 'Find Doctors',
    searchDoctors: 'Search doctors...',
    uploadPrescription: 'Upload Prescription',
    browsePharmacies: 'Browse Pharmacies',
    emergency: 'Emergency',
    call: 'Call',
    governmentDoctor: 'Government',
    independentDoctor: 'Independent',
    commercialDoctor: 'Commercial Hospital',
    governmentPharmacy: 'Jan Aushadhi',
    commercialPharmacy: 'Commercial',
  },
  hi: {
    home: 'होम',
    symptomEntry: 'लक्षण जांच',
    findDoctor: 'डॉक्टर खोजें',
    prescription: 'नुस्खा',
    profile: 'प्रोफ़ाइल',
    speakSymptoms: 'अपने लक्षण बोलें',
    typeSymptoms: 'या यहाँ अपने लक्षण टाइप करें...',
    analyzing: 'विश्लेषण हो रहा है...',
    findDoctors: 'डॉक्टर खोजें',
    searchDoctors: 'डॉक्टर खोजें...',
    uploadPrescription: 'प्रिस्क्रिप्शन अपलोड करें',
    browsePharmacies: 'फार्मेसी देखें',
    emergency: 'आपातकाल',
    call: 'कॉल करें',
    governmentDoctor: 'सरकारी',
    independentDoctor: 'स्वतंत्र',
    commercialDoctor: 'व्यावसायिक अस्पताल',
    governmentPharmacy: 'जन औषधि',
    commercialPharmacy: 'व्यावसायिक',
  },
};
