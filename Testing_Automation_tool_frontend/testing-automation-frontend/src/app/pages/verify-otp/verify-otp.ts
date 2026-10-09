import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren, inject } from '@angular/core';
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
export class VerifyOtp implements OnInit, OnDestroy {
  @ViewChildren('otpInput') otpInputElements!: QueryList<ElementRef<HTMLInputElement>>;

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);

  email = '';
  maskedEmail = '';
  otpDigits: string[] = ['', '', '', '', '', ''];
  isLoading = false;
  isResending = false;
  errorMessage = '';
  successMessage = '';

  timer = 120;
  timerInterval: any;
  canResend = false;

  ngOnInit(): void {
    const navState = history.state;
    this.email = navState?.email || this.route.snapshot.queryParams['email'] || '';
    this.maskedEmail = this.maskEmail(this.email);
    this.startTimer();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  startTimer(): void {
    this.timer = 120;
    this.canResend = false;
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.cdr.markForCheck();

    this.timerInterval = setInterval(() => {
      if (this.timer > 0) {
        this.timer--;
      } else {
        this.canResend = true;
        clearInterval(this.timerInterval);
      }
      this.cdr.markForCheck();
    }, 1000);
  }

  get formattedTimer(): string {
    const minutes = Math.floor(this.timer / 60);
    const seconds = this.timer % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  maskEmail(email: string): string {
    if (!email || !email.includes('@')) return 'your email';
    const [name, domain] = email.split('@');
    if (name.length <= 2) {
      return `${name[0]}*@${domain}`;
    }
    const visibleStart = name.slice(0, 2);
    const masked = '*'.repeat(Math.max(1, name.length - 3));
    const visibleEnd = name.slice(-1);
    return `${visibleStart}${masked}${visibleEnd}@${domain}`;
  }

  onDigitInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '');

    if (digits.length > 1) {
      const chars = digits.slice(0, 6).split('');
      chars.forEach((char, idx) => {
        if (index + idx < 6) {
          this.otpDigits[index + idx] = char;
        }
      });
      const inputs = this.otpInputElements.toArray();
      inputs.forEach((inputEl, idx) => {
        inputEl.nativeElement.value = this.otpDigits[idx] || '';
      });
      const targetFocus = Math.min(index + chars.length, 5);
      inputs[targetFocus]?.nativeElement.focus();
      if (this.isOtpComplete) {
        this.verifyOtp();
      }
      return;
    }

    const digit = digits.slice(-1);
    this.otpDigits[index] = digit;
    input.value = digit;

    if (digit && index < this.otpDigits.length - 1) {
      const nextInput = this.otpInputElements.toArray()[index + 1];
      nextInput?.nativeElement.focus();
    }
  }

  onKeyDown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace') {
      if (!this.otpDigits[index] && index > 0) {
        event.preventDefault();
        this.otpDigits[index - 1] = '';
        const prevInput = this.otpInputElements.toArray()[index - 1];
        if (prevInput) {
          prevInput.nativeElement.value = '';
          prevInput.nativeElement.focus();
        }
      } else {
        this.otpDigits[index] = '';
      }
    } else if (event.key === 'ArrowLeft' && index > 0) {
      event.preventDefault();
      this.otpInputElements.toArray()[index - 1]?.nativeElement.focus();
    } else if (event.key === 'ArrowRight' && index < 5) {
      event.preventDefault();
      this.otpInputElements.toArray()[index + 1]?.nativeElement.focus();
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pastedData = event.clipboardData?.getData('text') || '';
    const digitsOnly = pastedData.replace(/\D/g, '').slice(0, 6);

    if (digitsOnly.length > 0) {
      for (let i = 0; i < 6; i++) {
        this.otpDigits[i] = digitsOnly[i] || '';
      }

      const inputs = this.otpInputElements.toArray();
      inputs.forEach((inputEl, idx) => {
        inputEl.nativeElement.value = this.otpDigits[idx] || '';
      });

      const focusIdx = Math.min(digitsOnly.length, 5);
      inputs[focusIdx]?.nativeElement.focus();

      if (this.isOtpComplete) {
        this.verifyOtp();
      }
    }
  }

  get isOtpComplete(): boolean {
    return this.otpDigits.every((d) => d !== '' && /^\d$/.test(d));
  }

  get currentOtp(): string {
    return this.otpDigits.join('');
  }

  verifyOtp(): void {
    if (!this.isOtpComplete || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const otp = this.currentOtp;

    this.authService.verifyOtp({ email: this.email, otp }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response?.message ?? 'OTP verified successfully!';
        console.log(response);
        const resetToken = response.data?.resetToken || '87';
        console.log(resetToken);
        this.cdr.markForCheck();
    
        setTimeout(() => {
          this.router.navigate(['/reset-password'], {
            state: { resetToken }
          });
        }, 800);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error?.error?.message ?? 'Invalid or expired OTP. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }

  resendOtp(): void {
    if (!this.canResend || this.isResending) return;

    this.isResending = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.markForCheck();

    this.authService.sendPasswordResetOtp({ email: this.email }).subscribe({
      next: (response) => {
        this.isResending = false;
        this.successMessage = response?.message ?? 'A new verification code has been sent!';
        this.startTimer();
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.isResending = false;
        this.errorMessage = error?.error?.message ?? 'Failed to resend OTP. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }
}
