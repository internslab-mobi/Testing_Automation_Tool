import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChildren,
  inject
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/authService';

@Component({
  standalone: true,
  selector: 'app-verify-otp',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './verify-otp.html',
  styleUrl: './verify-otp.css'
})
export class VerifyOtp implements OnInit, AfterViewInit, OnDestroy {
  @ViewChildren('otpInput') otpInputElements!: QueryList<ElementRef<HTMLInputElement>>;

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = '';
  isLoading = false;
  isResending = false;
  isSubmitted = false;
  errorMessage = '';
  successMessage = '';

  readonly digitControlNames = [
    'digit0',
    'digit1',
    'digit2',
    'digit3',
    'digit4',
    'digit5'
  ] as const;

  verifyOtpForm = new FormGroup({
    digit0: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)]),
    digit1: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)]),
    digit2: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)]),
    digit3: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)]),
    digit4: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)]),
    digit5: new FormControl('', [Validators.required, Validators.pattern(/^\d$/)])
  });

  readonly expirySeconds = 60; // 1 minute expiry as specified in authentication flow
  timer = 60;
  private timerInterval?: ReturnType<typeof setInterval>;
  canResend = false;

  ngOnInit(): void {
    const navState = history.state;
    this.email = navState?.email || this.route.snapshot.queryParams['email'] || '';
    this.startTimer();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      const firstInput = this.otpInputElements?.first;
      firstInput?.nativeElement.focus();
    }, 100);
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  /**
   * Masks email dynamically (e.g. mithuna@gmail.com -> mit***@gmail.com).
   * Ensures the full email is never displayed.
   */

get maskedEmail(): string {
  if (!this.email || !this.email.includes('@')) {
    return '';
  }

  const [user, domain] = this.email.split('@');

  if (!user || !domain) {
    return '';
  }

  return `${user.substring(0, 5)}@${domain}`;
}

  startTimer(seconds: number = this.expirySeconds): void {
    this.timer = seconds;
    this.canResend = false;
    this.clearTimer();

    this.timerInterval = setInterval(() => {
      if (this.timer > 0) {
        this.timer--;
      } else {
        this.canResend = true;
        this.clearTimer();
      }
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = undefined;
    }
  }

  get formattedTimer(): string {
    const minutes = Math.floor(this.timer / 60);
    const seconds = this.timer % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  get otpValue(): string {
    const values = this.verifyOtpForm.value;
    return [
      values.digit0 ?? '',
      values.digit1 ?? '',
      values.digit2 ?? '',
      values.digit3 ?? '',
      values.digit4 ?? '',
      values.digit5 ?? ''
    ].join('');
  }

  get isOtpComplete(): boolean {
    return this.verifyOtpForm.valid && this.otpValue.length === 6;
  }

  onDigitInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const cleanDigits = input.value.replace(/\D/g, '');

    // Support typing or auto-completing multiple digits
    if (cleanDigits.length > 1) {
      const chars = cleanDigits.slice(0, 6).split('');
      const inputs = this.otpInputElements.toArray();
      chars.forEach((char, idx) => {
        if (index + idx < 6) {
          const controlName = this.digitControlNames[index + idx];
          this.verifyOtpForm.get(controlName)?.setValue(char);
          if (inputs[index + idx]) {
            inputs[index + idx].nativeElement.value = char;
          }
        }
      });
      const targetFocus = Math.min(index + chars.length, 5);
      inputs[targetFocus]?.nativeElement.focus();

      this.errorMessage = '';
      if (this.isOtpComplete) {
        this.verifyOtp();
      }
      return;
    }

    const char = cleanDigits.length > 0 ? cleanDigits.slice(-1) : '';
    const controlName = this.digitControlNames[index];
    this.verifyOtpForm.get(controlName)?.setValue(char);
    input.value = char;

    this.errorMessage = '';

    if (char && index < 5) {
      const inputs = this.otpInputElements.toArray();
      const nextInput = inputs[index + 1]?.nativeElement;
      if (nextInput) {
        nextInput.focus();
        nextInput.select();
      }
    }

    if (this.isOtpComplete) {
      this.verifyOtp();
    }
  }

  onKeyDown(index: number, event: KeyboardEvent): void {
    const inputs = this.otpInputElements.toArray();

    if (event.key === 'Backspace') {
      const controlName = this.digitControlNames[index];
      const currentVal = this.verifyOtpForm.get(controlName)?.value;

      if (!currentVal && index > 0) {
        event.preventDefault();
        const prevControl = this.digitControlNames[index - 1];
        this.verifyOtpForm.get(prevControl)?.setValue('');
        const prevInput = inputs[index - 1]?.nativeElement;
        if (prevInput) {
          prevInput.value = '';
          prevInput.focus();
        }
      } else {
        this.verifyOtpForm.get(controlName)?.setValue('');
      }
    } else if (event.key === 'ArrowLeft' && index > 0) {
      event.preventDefault();
      inputs[index - 1]?.nativeElement.focus();
    } else if (event.key === 'ArrowRight' && index < 5) {
      event.preventDefault();
      inputs[index + 1]?.nativeElement.focus();
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pastedData = event.clipboardData?.getData('text') || '';
    const digitsOnly = pastedData.replace(/\D/g, '').slice(0, 6);

    if (digitsOnly.length > 0) {
      const inputs = this.otpInputElements.toArray();
      for (let i = 0; i < 6; i++) {
        const val = digitsOnly[i] || '';
        const controlName = this.digitControlNames[i];
        this.verifyOtpForm.get(controlName)?.setValue(val);
        if (inputs[i]) {
          inputs[i].nativeElement.value = val;
        }
      }

      const focusIndex = Math.min(digitsOnly.length, 5);
      inputs[focusIndex]?.nativeElement.focus();

      this.errorMessage = '';

      if (this.isOtpComplete) {
        this.verifyOtp();
      }
    }
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.verifyOtpForm.invalid) {
      this.verifyOtpForm.markAllAsTouched();
      return;
    }
    this.verifyOtp();
  }

  verifyOtp(): void {
    if (!this.isOtpComplete || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const otp = this.otpValue;

    this.authService.verifyOtp({ email: this.email, otp }).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.successMessage = response?.message ?? 'OTP verified successfully!';
        const resetToken = response?.resetToken || response?.data?.resetToken;

        setTimeout(() => {
          this.router.navigate(['/reset-password'], {
            state: { resetToken, email: this.email }
          });
        }, 800);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage =
          error?.error?.message ?? 'Invalid or expired OTP. Please try again.';
      }
    });
  }

  resendOtp(): void {
    if (!this.canResend || this.isResending) {
      return;
    }

    if (!this.email) {
      this.errorMessage =
        'Email address not found. Please return to the Forgot Password page to request an OTP.';
      return;
    }

    this.isResending = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.sendPasswordResetOtp({ email: this.email }).subscribe({
      next: (response) => {
        this.isResending = false;
        this.successMessage =
          response?.message ?? 'A new verification code has been sent to your email.';
        this.resetInputs();
        this.startTimer();
      },
      error: (error) => {
        this.isResending = false;
        this.errorMessage =
          error?.error?.message ?? 'Failed to resend OTP. Please try again.';
      }
    });
  }

  private resetInputs(): void {
    this.verifyOtpForm.reset();
    const inputs = this.otpInputElements?.toArray() ?? [];
    inputs.forEach((inputEl) => {
      if (inputEl.nativeElement) {
        inputEl.nativeElement.value = '';
      }
    });
    inputs[0]?.nativeElement?.focus();
  }
}
