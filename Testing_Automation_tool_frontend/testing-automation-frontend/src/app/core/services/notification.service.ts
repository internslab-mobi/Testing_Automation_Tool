import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { InAppNotification } from '../../model/workspace/workspace.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getNotifications(): Observable<InAppNotification[]> {
    return this.http.get<any>(`${this.baseUrl}/in-app-notifications`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        const list = Array.isArray(raw) ? raw : [];
        return list.map((item: any) => ({
          notificationId: item.notificationId,
          message: item.message,
          type: item.type || (item.message?.toLowerCase().includes('fixed') ? 'FIXED' : item.message?.toLowerCase().includes('comment') ? 'COMMENT' : 'ALERT'),
          notificationStatus: item.notificationStatus || 'UNREAD',
          bugId: item.bugId,
          bugFormatId: item.bugFormatId,
          createdAt: item.createdAt
        } as InAppNotification));
      }),
      catchError(() => of([]))
    );
  }

  markAsRead(notificationId: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/in-app-notifications/${notificationId}`, {
      status: 'READ'
    });
  }

  deleteNotification(notificationId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/in-app-notifications/${notificationId}`);
  }
}
