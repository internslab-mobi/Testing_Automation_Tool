import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { VerifyOtp } from './verify-otp';

describe('VerifyOtp', () => {
  let component: VerifyOtp;
  let fixture: ComponentFixture<VerifyOtp>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerifyOtp],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyOtp);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with 6 empty digit controls', () => {
    expect(component.verifyOtpForm).toBeDefined();
    expect(component.digitControlNames.length).toBe(6);
    expect(component.verifyOtpForm.valid).toBe(false);
  });

  it('should be invalid when incomplete OTP is provided', () => {
    component.verifyOtpForm.patchValue({
      digit0: '1',
      digit1: '2',
      digit2: '3'
    });
    expect(component.isOtpComplete).toBe(false);
    expect(component.verifyOtpForm.valid).toBe(false);
  });

  it('should be valid when all 6 digits are provided', () => {
    component.verifyOtpForm.patchValue({
      digit0: '1',
      digit1: '2',
      digit2: '3',
      digit3: '4',
      digit4: '5',
      digit5: '6'
    });
    expect(component.isOtpComplete).toBe(true);
    expect(component.verifyOtpForm.valid).toBe(true);
    expect(component.otpValue).toBe('123456');
  });

  it('should format timer into mm:ss', () => {
    component.timer = 60;
    expect(component.formattedTimer).toBe('01:00');
    component.timer = 45;
    expect(component.formattedTimer).toBe('00:45');
  });
});
