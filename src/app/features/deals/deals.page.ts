import { Component, OnInit, ViewChild } from '@angular/core';
import { GameProviderService } from '../../shared/services/game.provider';
import { Preferences } from '@capacitor/preferences';
import { Browser } from '@capacitor/browser';
import { IonModal } from '@ionic/angular';

@Component({
  standalone: false,
  selector: 'app-deals',
  templateUrl: './deals.page.html',
  styleUrls: ['./deals.page.scss'],
})
export class DealsPage implements OnInit {
  @ViewChild('dealModal') modal!: IonModal;
  
  topDeals: any[] = [];
  searchResults: any[] = [];
  isLoading = true;
  searchActive = false;
  selectedDeal: any = null;
  currentFavoriteId: string | null = null; 

  storesMap: { [key: string]: string } = {}; 
  storeIconMap: { [key: string]: string } = {};

  constructor(private gameProvider: GameProviderService) {}

  ngOnInit() {
    this.loadTopDeals();
    this.loadFavoriteId();
    this.loadStores();
  }

  ionViewWillEnter() {
    this.loadFavoriteId();
  }

  async loadFavoriteId() {
    const { value } = await Preferences.get({ key: 'favoriteGame' });
    if (value) {
      const fav = JSON.parse(value);
      this.currentFavoriteId = fav.gameID || fav.dealID;
    } else {
      this.currentFavoriteId = null;
    }
  }

  loadStores() {
    this.gameProvider.getStores().subscribe((storesRes: any[]) => {
      storesRes.forEach(store => {
        this.storesMap[store.storeID] = store.storeName;
        if (store.images?.icon) {
          this.storeIconMap[store.storeID] = `https://www.cheapshark.com${store.images.icon}`;
        }
      });

      this.topDeals = this.applyStoreMetadata(this.topDeals);
      this.searchResults = this.applyStoreMetadata(this.searchResults);
    });
  }

  loadTopDeals() {
    this.gameProvider.getTopDeals().subscribe((dealsRes: any) => {
      this.topDeals = this.applyStoreMetadata(dealsRes);
      this.isLoading = false;
    });
  }

  handleSearch(query: string) {
    if (!query || query.trim() === '') {
      this.searchActive = false;
      this.searchResults = [];
      return;
    }
    
    this.searchActive = true;
    this.isLoading = true;
    this.gameProvider.searchDeals(query).subscribe((res: any) => {
      this.searchResults = this.applyStoreMetadata(res);
      this.isLoading = false;
    });
  }

  applyStoreMetadata(deals: any[]): any[] {
    if (!Array.isArray(deals) || deals.length === 0) {
      return deals;
    }

    return deals.map(deal => ({
      ...deal,
      storeName: this.storesMap[deal.storeID] || deal.storeName,
      storeIcon: this.storeIconMap[deal.storeID] || deal.storeIcon,
    }));
  }

  async toggleFavorite(game: any) {
    const gameId = game.gameID || game.dealID;

    if (this.currentFavoriteId === gameId) {
      await Preferences.remove({ key: 'favoriteGame' });
      this.currentFavoriteId = null;
    } else {
      this.currentFavoriteId = gameId;

      this.gameProvider.getGameDetails(gameId).subscribe(async (details: any) => {
        
        const realDeals = details.deals.map((d: any) => ({
          storeID: d.storeID,
          storeName: this.storesMap[d.storeID] || `Store ${d.storeID}`,
          storeIcon: this.storeIconMap[d.storeID] || '',
          salePrice: d.price,       
          normalPrice: d.retailPrice, 
          savings: d.savings
        }));

        const gameToSave = {
          ...game,
          deals: realDeals 
        };

        await Preferences.set({
          key: 'favoriteGame',
          value: JSON.stringify(gameToSave)
        });
      });
    }
  }

  checkIsFavorite(game: any): boolean {
    const gameId = game.gameID || game.dealID;
    return this.currentFavoriteId === gameId;
  }

  openDealDetails(deal: any) {
    this.selectedDeal = deal;
    this.modal.present();
    setTimeout(() => {
      this.modal.setCurrentBreakpoint(1);
    }, 100);
  }

  async openBrowser(dealID: string) {
    const toolbarColor = '#001D39';
    await Browser.open({ 
      url: `https://www.cheapshark.com/redirect?dealID=${dealID}`,
      toolbarColor: toolbarColor,
    });
  }
}