import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SurveySection from '../../widget/survey/SurveySection';
import SurveyRadioQuestion from '../../widget/survey/SurveyRadioQuestion';
import SurveyCheckboxQuestion from '../../widget/survey/SurveyCheckboxQuestion';
import SurveyTextInput from '../../widget/survey/SurveyTextInput';
import AgreementCheckboxes from '../../widget/survey/AgreementCheckboxes';
import {
  ageOptions,
  incomeOptions,
  assetOptions,
  investmentRatioOptions,
  emergencyOptions,
  purposeOptions,
  periodOptions,
  experienceOptions,
  productOptions,
  maxLossOptions,
  styleOptions,
  declineOptions,
  liquidityOptions,
  agreements,
} from './surveyOptions';
import styles from './SignupPage.module.css';

interface SurveyData {
  nickname: string;
  age: string;
  income: string;
  assets: string;
  investmentRatio: string;
  emergency: string;
  purpose: string;
  period: string;
  experience: string;
  products: string[];
  maxLoss: string;
  style: string;
  decline: string;
  liquidity: string;
  riskAwareness: boolean;
  resultAgreement: boolean;
  decisionConfirmation: boolean;
}

export default function SignupPage() {
  const navigate = useNavigate();
  const [surveyData, setSurveyData] = useState<SurveyData>({
    nickname: '',
    age: '',
    income: '',
    assets: '',
    investmentRatio: '',
    emergency: '',
    purpose: '',
    period: '',
    experience: '',
    products: [],
    maxLoss: '',
    style: '',
    decline: '',
    liquidity: '',
    riskAwareness: false,
    resultAgreement: false,
    decisionConfirmation: false,
  });

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

  const isFormValid = () => {
    return (
      surveyData.nickname &&
      surveyData.age &&
      surveyData.income &&
      surveyData.assets &&
      surveyData.investmentRatio &&
      surveyData.emergency &&
      surveyData.purpose &&
      surveyData.period &&
      surveyData.experience &&
      surveyData.products.length > 0 &&
      surveyData.maxLoss &&
      surveyData.style &&
      surveyData.decline &&
      surveyData.liquidity &&
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
          <SurveySection title='기본 정보'>
            <SurveyTextInput
              label='제가 어떻게 불러드리면 좋을까요?'
              placeholder='닉네임을 입력해주세요'
              value={surveyData.nickname}
              onChange={(value) => handleInputChange('nickname', value)}
            />
            <SurveyRadioQuestion
              label='현재 나이가 어떻게 되시나요?'
              name='age'
              options={ageOptions}
              value={surveyData.age}
              onChange={(value) => handleInputChange('age', value)}
            />
            <SurveyRadioQuestion
              label='연간 소득은 어느 정도 되시나요?'
              name='income'
              options={incomeOptions}
              value={surveyData.income}
              onChange={(value) => handleInputChange('income', value)}
            />
          </SurveySection>

          <SurveySection title='재산 상황'>
            <SurveyRadioQuestion
              label='현재 보유하고 계신 금융자산은 얼마나 되시나요?'
              name='assets'
              options={assetOptions}
              value={surveyData.assets}
              onChange={(value) => handleInputChange('assets', value)}
            />
            <SurveyRadioQuestion
              label='전체 자산 중 얼마나 투자하실 예정이신가요?'
              name='investmentRatio'
              options={investmentRatioOptions}
              value={surveyData.investmentRatio}
              onChange={(value) => handleInputChange('investmentRatio', value)}
            />
            <SurveyRadioQuestion
              label='생활비 3개월분의 비상자금을 준비해두고 계시나요?'
              name='emergency'
              options={emergencyOptions}
              value={surveyData.emergency}
              onChange={(value) => handleInputChange('emergency', value)}
            />
          </SurveySection>

          <SurveySection title='투자 목적 및 기간'>
            <SurveyRadioQuestion
              label='어떤 목적으로 투자를 계획하고 계시나요?'
              name='purpose'
              options={purposeOptions}
              value={surveyData.purpose}
              onChange={(value) => handleInputChange('purpose', value)}
            />
            <SurveyRadioQuestion
              label='얼마나 오랫동안 투자하실 계획이신가요?'
              name='period'
              options={periodOptions}
              value={surveyData.period}
              onChange={(value) => handleInputChange('period', value)}
            />
          </SurveySection>

          <SurveySection title='투자 경험'>
            <SurveyRadioQuestion
              label='금융 투자 경험이 어느 정도 되시나요?'
              name='experience'
              options={experienceOptions}
              value={surveyData.experience}
              onChange={(value) => handleInputChange('experience', value)}
            />
            <SurveyCheckboxQuestion
              label='어떤 금융상품에 투자해보셨나요? (복수선택 가능)'
              options={productOptions}
              selectedValues={surveyData.products}
              onChange={handleProductChange}
            />
          </SurveySection>

          <SurveySection title='위험 감내도'>
            <SurveyRadioQuestion
              label='투자 시 최대 어느 정도의 손실까지 받아들이실 수 있나요?'
              name='maxLoss'
              options={maxLossOptions}
              value={surveyData.maxLoss}
              onChange={(value) => handleInputChange('maxLoss', value)}
            />
            <SurveyRadioQuestion
              label='본인의 투자 성향을 어떻게 생각하시나요?'
              name='style'
              options={styleOptions}
              value={surveyData.style}
              onChange={(value) => handleInputChange('style', value)}
            />
            <SurveyRadioQuestion
              label='만약 투자자산이 20% 하락한다면 어떻게 하시겠나요?'
              name='decline'
              options={declineOptions}
              value={surveyData.decline}
              onChange={(value) => handleInputChange('decline', value)}
            />
          </SurveySection>

          <SurveySection title='유동성'>
            <SurveyRadioQuestion
              label='투자 중에 갑자기 돈이 필요할 가능성이 어느 정도 되시나요?'
              name='liquidity'
              options={liquidityOptions}
              value={surveyData.liquidity}
              onChange={(value) => handleInputChange('liquidity', value)}
            />
          </SurveySection>

          <SurveySection title='필수 동의사항'>
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
            <button type='submit' className={styles.submitButton} disabled={false /*!isFormValid()*/}>
              제출하기
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
