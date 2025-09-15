import { useState } from 'react';

export const useModalState = () => {
  const [isOpen, setIsOpen] = useState(false);

  const open = () => setIsOpen(true);
  const close = () => setIsOpen(false);
  const toggle = () => setIsOpen(prev => !prev);

  return {
    isOpen,
    open,
    close,
    toggle
  };
};

export const useConfirmModal = () => {
  const [confirmState, setConfirmState] = useState({
    isOpen: false,
    title: '',
    message: '',
    type: 'default' as 'default' | 'danger' | 'warning',
    confirmText: '확인',
    cancelText: '취소',
    onConfirm: () => {}
  });

  const showConfirm = (config: {
    title: string;
    message: string;
    type?: 'default' | 'danger' | 'warning';
    confirmText?: string;
    cancelText?: string;
    onConfirm: () => void;
  }) => {
    setConfirmState({
      isOpen: true,
      title: config.title,
      message: config.message,
      type: config.type || 'default',
      confirmText: config.confirmText || '확인',
      cancelText: config.cancelText || '취소',
      onConfirm: config.onConfirm
    });
  };

  const hideConfirm = () => {
    setConfirmState(prev => ({ ...prev, isOpen: false }));
  };

  return {
    confirmState,
    showConfirm,
    hideConfirm
  };
};