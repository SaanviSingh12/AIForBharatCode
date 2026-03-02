import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { ArrowLeft, Upload, Camera, Search, FileText, Volume2 } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { useTranslation } from '../i18n';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { BottomNav } from '../components/BottomNav';
import { analyzePrescription as apiAnalyzePrescription, playAudioResponse } from '../services/api';

export const PrescriptionSearch: React.FC = () => {
  const navigate = useNavigate();
  const { language, setPrescription, setPrescriptionResult, setIsLoading, setApiError } = useApp();
  const t = useTranslation();

  const [searchText, setSearchText] = useState('');
  const [uploadedImage, setUploadedImage] = useState<string | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleImageUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Show preview
    const reader = new FileReader();
    reader.onloadend = () => setUploadedImage(reader.result as string);
    reader.readAsDataURL(file);

    setIsProcessing(true);
    setIsLoading(true);
    setErrorMsg(null);

    try {
      const result = await apiAnalyzePrescription(file, language);
      setPrescriptionResult(result);

      if (result.success) {
        setPrescription(result.extractedText || '');
        navigate('/pharmacy-results', { state: { fromPrescription: true } });
      } else {
        setErrorMsg(result.error || 'Failed to process prescription');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Network error';
      setErrorMsg(msg);
      setApiError(msg);
      // Fallback: still navigate with mock data
      setPrescription(searchText || 'Prescription uploaded');
      navigate('/pharmacy-results', { state: { fromPrescription: true } });
    } finally {
      setIsProcessing(false);
      setIsLoading(false);
    }
  };

  const handleTextSearch = () => {
    if (searchText.trim()) {
      setPrescription(searchText);
      navigate('/pharmacy-results', { state: { fromPrescription: true } });
    }
  };

  const handleBrowsePharmacies = () => {
    navigate('/pharmacy-results', { state: { fromPrescription: false } });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
      {/* Header */}
      <div className="bg-white shadow-sm p-4">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate('/home')}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="font-semibold text-lg">{t.prescription_title}</h1>
        </div>
      </div>

      <div className="p-6 space-y-6">
        {/* Upload Prescription */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
              <Upload className="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">{t.prescription_upload_title}</h2>
              <p className="text-sm text-gray-600">{t.prescription_upload_subtitle}</p>
            </div>
          </div>

          {errorMsg && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-700 text-sm">{errorMsg}</p>
            </div>
          )}

          {uploadedImage ? (
            <div className="mb-4">
              <img
                src={uploadedImage}
                alt="Uploaded prescription"
                className="w-full rounded-lg border-2 border-gray-200"
              />
              {isProcessing && (
                <div className="mt-4 bg-blue-50 rounded-lg p-4 text-center">
                  <div className="animate-pulse">
                    <p className="text-blue-600 font-semibold">{t.prescription_processing}</p>
                    <p className="text-sm text-blue-600 mt-1">{t.prescription_processing_ai}</p>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div>
              <label htmlFor="camera-upload" className="block w-full">
                <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center cursor-pointer hover:border-blue-500 hover:bg-blue-50 transition-all">
                  <Camera className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                  <p className="text-gray-700 font-semibold mb-1">{t.prescription_take_photo}</p>
                  <p className="text-sm text-gray-500">{t.prescription_photo_subtitle}</p>
                </div>
              </label>
              <input
                id="camera-upload"
                type="file"
                accept="image/*"
                capture="environment"
                onChange={handleImageUpload}
                className="hidden"
              />
            </div>
          )}
        </Card>

        {/* Text Search */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
              <FileText className="w-6 h-6 text-green-600" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">{t.prescription_type_title}</h2>
              <p className="text-sm text-gray-600">{t.prescription_type_subtitle}</p>
            </div>
          </div>

          <div className="space-y-3">
            <Input
              placeholder={t.prescription_type_placeholder}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleTextSearch()}
            />
            <Button
              onClick={handleTextSearch}
              className="w-full bg-green-600 hover:bg-green-700"
              disabled={!searchText.trim()}
            >
              <Search className="w-4 h-4 mr-2" />
              {t.prescription_search_btn}
            </Button>
          </div>
        </Card>

        {/* Browse Pharmacies */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center">
              <Search className="w-6 h-6 text-purple-600" />
            </div>
            <div>
              <h2 className="font-semibold text-gray-900">{t.prescription_browse_title}</h2>
              <p className="text-sm text-gray-600">{t.prescription_browse_subtitle}</p>
            </div>
          </div>
          <Button onClick={handleBrowsePharmacies} variant="outline" className="w-full">
            {t.prescription_view_all}
          </Button>
        </Card>

        {/* Info Card */}
        <Card className="p-4 bg-green-50 border-green-200">
          <h3 className="font-semibold text-green-900 mb-2">{t.prescription_savings_title}</h3>
          <p className="text-sm text-green-800 mb-3">
            {t.prescription_savings_desc}
          </p>
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="bg-white rounded-lg p-3">
              <p className="text-gray-600">{t.prescription_example_paracetamol}</p>
              <p className="font-semibold text-green-700">₹8 vs ₹50</p>
            </div>
            <div className="bg-white rounded-lg p-3">
              <p className="text-gray-600">{t.prescription_example_metformin}</p>
              <p className="font-semibold text-green-700">₹25 vs ₹150</p>
            </div>
          </div>
        </Card>
      </div>

      <BottomNav />
    </div>
  );
};
