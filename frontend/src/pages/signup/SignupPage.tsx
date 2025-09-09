import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SurveySection from '../../widget/survey/SurveySection';
import SurveyRadioQuestion from '../../widget/survey/SurveyRadioQuestion';
import SurveyCheckboxQuestion from '../../widget/survey/SurveyCheckboxQuestion';
import SurveyTextInput from '../../widget/survey/SurveyTextInput';
import AgreementCheckboxes from '../../widget/survey/AgreementCheckboxes';
import {
  ageOptions,
  incomeSourceOptions,
  purposeOptions,
  periodOptions,
  experienceOptions,
  productOptions,
  maxLossOptions,
  styleOptions,
  agreements,
} from './surveyOptions';
import styles from './SignupPage.module.css';

interface SurveyData {
  nickname: string;
  age: string;
  incomeSource: string;
  purpose: string;
  period: string;
  experience: string;
  products: string[];
  maxLoss: string;
  style: string;
  riskAwareness: boolean;
  resultAgreement: boolean;
  decisionConfirmation: boolean;
}

type DuplicateCheckStatus = 'none' | 'checking' | 'available' | 'unavailable';

export default function SignupPage() {
  const navigate = useNavigate();
  const [surveyData, setSurveyData] = useState<SurveyData>({
    nickname: '',
    age: '',
    incomeSource: '',
    purpose: '',
    period: '',
    experience: '',
    products: [],
    maxLoss: '',
    style: '',
    riskAwareness: false,
    resultAgreement: false,
    decisionConfirmation: false,
  });

  const [duplicateCheckStatus, setDuplicateCheckStatus] = useState<DuplicateCheckStatus>('none');

  const handleInputChange = (field: keyof SurveyData, value: string | boolean) => {
    setSurveyData((prev) => ({ ...prev, [field]: value }));
  };

  const handleProductChange = (product: string, checked: boolean) => {
    setSurveyData((prev) => ({
      ...prev,
      products: checked ? [...prev.products, product] : prev.products.filter((p) => p !== product),
    }));
  };

  const handleAgreementChange = (key: string, checked: boolean) => {
    setSurveyData((prev) => ({ ...prev, [key]: checked }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Survey Data:', surveyData);
    navigate('/dashboard');
  };

  const handleNicknameChange = (value: string) => {
    handleInputChange('nickname', value);
    if (duplicateCheckStatus !== 'none') {
      setDuplicateCheckStatus('none');
    }
  };

  const handleDuplicateCheck = async () => {
    if (!surveyData.nickname) return;

    setDuplicateCheckStatus('checking');

    // 시뮬레이션: 실제로는 API 호출
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 간단한 시뮬레이션: 'admin', 'test' 닉네임은 중복으로 처리
    const unavailableNicknames = ['admin', 'test', 'user'];
    const isAvailable = !unavailableNicknames.includes(surveyData.nickname.toLowerCase());

    setDuplicateCheckStatus(isAvailable ? 'available' : 'unavailable');
  };

  const isFormValid = () => {
    return (
      surveyData.nickname &&
      duplicateCheckStatus === 'available' &&
      surveyData.age &&
      surveyData.incomeSource &&
      surveyData.purpose &&
      surveyData.period &&
      surveyData.experience &&
      surveyData.products.length > 0 &&
      surveyData.maxLoss &&
      surveyData.style &&
      surveyData.riskAwareness &&
      surveyData.resultAgreement &&
      surveyData.decisionConfirmation
    );
  };

  return (
    <div className={styles.signupPage}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h1>투자자 성향 분석 설문지</h1>
          <p>시작하기에 앞서 더 나은 투자 추천을 위해 아래 질문에 대해 답해주세요</p>
        </div>

        <form onSubmit={handleSubmit} className={styles.surveyForm}>
          <SurveySection title='기본 정보' required={true}>
            <SurveyTextInput
              label=''
              placeholder='닉네임을 입력해주세요'
              value={surveyData.nickname}
              onChange={handleNicknameChange}
              showDuplicateCheck={true}
              onDuplicateCheck={handleDuplicateCheck}
              duplicateCheckStatus={duplicateCheckStatus}
            />
            <SurveyRadioQuestion
              label='현재 나이대는 어떻게 되시나요?'
              name='age'
              options={ageOptions}
              value={surveyData.age}
              onChange={(value) => handleInputChange('age', value)}
            />
            <SurveyRadioQuestion
              label='주요 소득원은 무엇인가요?'
              name='incomeSource'
              options={incomeSourceOptions}
              value={surveyData.incomeSource}
              onChange={(value) => handleInputChange('incomeSource', value)}
            />
          </SurveySection>

          <SurveySection title='투자 목적' required={true}>
            <SurveyRadioQuestion
              label='투자의 주된 목적은 무엇인가요?'
              name='purpose'
              options={purposeOptions}
              value={surveyData.purpose}
              onChange={(value) => handleInputChange('purpose', value)}
            />
            <SurveyRadioQuestion
              label='투자 기간은 어느 정도로 생각하시나요?'
              name='period'
              options={periodOptions}
              value={surveyData.period}
              onChange={(value) => handleInputChange('period', value)}
            />
          </SurveySection>

          <SurveySection title='투자 경험' required={true}>
            <SurveyCheckboxQuestion
              label='지금까지 투자해본 자산은 무엇인가요? (복수선택 가능)'
              options={productOptions}
              selectedValues={surveyData.products}
              onChange={handleProductChange}
            />
            <SurveyRadioQuestion
              label='금융 투자 경험은 얼마나 되셨나요?'
              name='experience'
              options={experienceOptions}
              value={surveyData.experience}
              onChange={(value) => handleInputChange('experience', value)}
            />
          </SurveySection>

          <SurveySection title='위험 감내도' required={true}>
            <SurveyRadioQuestion
              label='투자 원금에 손실이 발생할 경우, 감내할 수 있는 손실 수준은 어느 정도인가요?'
              name='maxLoss'
              options={maxLossOptions}
              value={surveyData.maxLoss}
              onChange={(value) => handleInputChange('maxLoss', value)}
            />
            <SurveyRadioQuestion
              label='다음 중 본인에게 가장 가까운 투자 태도는 무엇인가요?'
              name='style'
              options={styleOptions}
              value={surveyData.style}
              onChange={(value) => handleInputChange('style', value)}
            />
          </SurveySection>

          <SurveySection title='필수 동의사항' required={true}>
            <AgreementCheckboxes
              agreements={agreements}
              values={{
                riskAwareness: surveyData.riskAwareness,
                resultAgreement: surveyData.resultAgreement,
                decisionConfirmation: surveyData.decisionConfirmation,
              }}
              onChange={handleAgreementChange}
            />
          </SurveySection>

          <div className={styles.submitSection}>
            <button type='submit' className={styles.submitButton} disabled={!isFormValid()}>
              제출하기
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
