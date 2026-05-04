import { Injectable } from '@angular/core';
import { HttpService } from '../../core/services/http.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GameProviderService {

  constructor(private http: HttpService) { }

  getStores(): Observable<any> {
    return this.http.get('/stores');
  }

  getTopDeals(): Observable<any> {
    return this.http.get('/deals?pageSize=5');
  }

  searchDeals(query: string): Observable<any> {
    return this.http.get(`/deals?title=${query}`);
  }

  getGameDetails(gameId: string): Observable<any> {
    return this.http.get(`/games?id=${gameId}`);
  }
}